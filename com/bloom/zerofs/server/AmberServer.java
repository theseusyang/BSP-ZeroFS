/**
 * Copyright 2016 Bloom Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.bloom.zerofs.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloom.zerofs.api.clustermap.ClusterMap;
import com.bloom.zerofs.api.clustermap.DataNodeId;
import com.bloom.zerofs.api.config.ConnectionPoolConfig;
import com.bloom.zerofs.api.config.NetworkConfig;
import com.bloom.zerofs.api.config.ReplicationConfig;
import com.bloom.zerofs.api.config.SSLConfig;
import com.bloom.zerofs.api.config.ServerConfig;
import com.bloom.zerofs.api.config.StoreConfig;
import com.bloom.zerofs.api.config.VerifiableProperties;
import com.bloom.zerofs.api.network.ConnectionPool;
import com.bloom.zerofs.api.network.NetworkServer;
import com.bloom.zerofs.api.network.Port;
import com.bloom.zerofs.api.network.PortType;
import com.bloom.zerofs.api.notification.NotificationSystem;
import com.bloom.zerofs.api.store.FindTokenFactory;
import com.bloom.zerofs.api.store.StoreKeyFactory;
import com.bloom.zerofs.commons.LoggingNotificationSystem;
import com.bloom.zerofs.messageformat.BlobStoreHardDelete;
import com.bloom.zerofs.messageformat.BlobStoreRecovery;
import com.bloom.zerofs.network.BlockingChannelConnectionPool;
import com.bloom.zerofs.network.SocketServer;
import com.bloom.zerofs.replication.ReplicationManager;
import com.bloom.zerofs.store.StoreManager;
import com.bloom.zerofs.tools.Scheduler;
import com.bloom.zerofs.tools.SystemTime;
import com.bloom.zerofs.tools.Time;
import com.bloom.zerofs.tools.Utils;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;


/**
 * Amber server
 */
public class AmberServer {

  private CountDownLatch shutdownLatch = new CountDownLatch(1);
  private NetworkServer networkServer = null;
  private AmberRequests requests = null;
  private RequestHandlerPool requestHandlerPool = null;
  private Scheduler scheduler = null;
  private StoreManager storeManager = null;
  private ReplicationManager replicationManager = null;
  private Logger logger = LoggerFactory.getLogger(getClass());
  private final VerifiableProperties properties;
  private final ClusterMap clusterMap;
  private MetricRegistry registry = null;
  private JmxReporter reporter = null;
  private ConnectionPool connectionPool = null;
  private final NotificationSystem notificationSystem;
  private ServerMetrics metrics = null;
  private Time time;

  public AmberServer(VerifiableProperties properties, ClusterMap clusterMap, Time time)
      throws IOException {
    this(properties, clusterMap, new LoggingNotificationSystem(), time);
  }

  public AmberServer(VerifiableProperties properties, ClusterMap clusterMap, NotificationSystem notificationSystem,
      Time time)
      throws IOException {
    this.properties = properties;
    this.clusterMap = clusterMap;
    this.notificationSystem = notificationSystem;
    this.time = time;
  }

  public void startup()
      throws InstantiationException {
    try {
      logger.info("starting");
      logger.info("Setting up JMX.");
      long startTime = SystemTime.getInstance().milliseconds();
      registry = clusterMap.getMetricRegistry();
      this.metrics = new ServerMetrics(registry);
      reporter = JmxReporter.forRegistry(registry).build();
      reporter.start();

      logger.info("creating configs");
      NetworkConfig networkConfig = new NetworkConfig(properties);
      StoreConfig storeConfig = new StoreConfig(properties);
      ServerConfig serverConfig = new ServerConfig(properties);
      ReplicationConfig replicationConfig = new ReplicationConfig(properties);
      ConnectionPoolConfig connectionPoolConfig = new ConnectionPoolConfig(properties);
      SSLConfig sslConfig = new SSLConfig(properties);
      // 加载一系列配置, 验证此配置
      properties.verify();

      scheduler = new Scheduler(serverConfig.serverSchedulerNumOfthreads, false);
      scheduler.startup();
      logger.info("check if node exist in clustermap host {} port {}", networkConfig.hostName, networkConfig.port);
      DataNodeId nodeId = clusterMap.getDataNodeId(networkConfig.hostName, networkConfig.port);
      if (nodeId == null) {
        throw new IllegalArgumentException("The node " + networkConfig.hostName + ":" + networkConfig.port +
            "is not present in the clustermap. Failing to start the datanode");
      }
      // 启动存储管理器
      StoreKeyFactory storeKeyFactory = Utils.getObj(storeConfig.storeKeyFactory, clusterMap);
      FindTokenFactory findTokenFactory = Utils.getObj(replicationConfig.replicationTokenFactory, storeKeyFactory);
      storeManager =
          new StoreManager(storeConfig, scheduler, registry, clusterMap.getReplicaIds(nodeId), storeKeyFactory,
              new BlobStoreRecovery(), new BlobStoreHardDelete(), time);
      storeManager.start();
      // 启动连接池
      connectionPool = new BlockingChannelConnectionPool(connectionPoolConfig, sslConfig, registry);
      connectionPool.start();
      // 启动同步管理器
      replicationManager =
          new ReplicationManager(replicationConfig, sslConfig, storeConfig, storeManager, storeKeyFactory, clusterMap,
              scheduler, nodeId, connectionPool, registry, notificationSystem);
      replicationManager.start();
      // 添加端口
      ArrayList<Port> ports = new ArrayList<Port>();
      ports.add(new Port(networkConfig.port, PortType.PLAINTEXT));
      if (nodeId.hasSSLPort()) {
        ports.add(new Port(nodeId.getSSLPort(), PortType.SSL));
      }
      // 启动网络服务器
      networkServer = new SocketServer(networkConfig, sslConfig, registry, ports);
      requests =
          new AmberRequests(storeManager, networkServer.getRequestResponseChannel(), clusterMap, nodeId, registry,
              findTokenFactory, notificationSystem, replicationManager, storeKeyFactory);
      requestHandlerPool = new RequestHandlerPool(serverConfig.serverRequestHandlerNumOfThreads,
          networkServer.getRequestResponseChannel(), requests);
      networkServer.start();
      // 服务器正常启动
      logger.info("started");
      long processingTime = SystemTime.getInstance().milliseconds() - startTime;
      metrics.serverStartTimeInMs.update(processingTime);
      logger.info("Server startup time in Ms " + processingTime);
    } catch (Exception e) {
      logger.error("Error during startup", e);
      throw new InstantiationException("failure during startup " + e);
    }
  }

  public void shutdown() {

    long startTime = SystemTime.getInstance().milliseconds();
    try {
      logger.info("shutdown started");

      if (scheduler != null) {
        scheduler.shutdown();
      }
      if (networkServer != null) {
        networkServer.shutdown();
      }
      if (requestHandlerPool != null) {
        requestHandlerPool.shutdown();
      }
      if (replicationManager != null) {
        replicationManager.shutdown();
      }
      if (storeManager != null) {
        storeManager.shutdown();
      }
      if (connectionPool != null) {
        connectionPool.shutdown();
      }
      if (reporter != null) {
        reporter.stop();
      }
      if (notificationSystem != null) {
        try {
          notificationSystem.close();
        } catch (IOException e) {
          logger.error("Error while closing notification system.", e);
        }
      }
      logger.info("shutdown completed");
    } catch (Exception e) {
      logger.error("Error while shutting down server", e);
    } finally {
      shutdownLatch.countDown();
      long processingTime = SystemTime.getInstance().milliseconds() - startTime;
      metrics.serverShutdownTimeInMs.update(processingTime);
      logger.info("Server shutdown time in Ms " + processingTime);
    }
  }

  public void awaitShutdown()
      throws InterruptedException {
    shutdownLatch.await();
  }
}
