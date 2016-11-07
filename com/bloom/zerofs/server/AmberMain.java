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

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloom.zerofs.api.clustermap.ClusterMap;
import com.bloom.zerofs.api.config.ClusterMapConfig;
import com.bloom.zerofs.api.config.VerifiableProperties;
import com.bloom.zerofs.clustermap.ClusterMapManager;
import com.bloom.zerofs.tools.InvocationOptions;
import com.bloom.zerofs.tools.SystemTime;
import com.bloom.zerofs.tools.Utils;


/**
 * Start point for creating an instance of {@link AmberServer} and starting/shutting it down.
 */
public class AmberMain {
  private static Logger logger = LoggerFactory.getLogger(AmberMain.class);

  public static void main(String[] args) {
    final AmberServer AmberServer;
    int exitCode = 0;
    try {
      final InvocationOptions options = new InvocationOptions(args);
      final Properties properties = Utils.loadProps(options.serverPropsFilePath);
      final VerifiableProperties verifiableProperties = new VerifiableProperties(properties);
      final ClusterMap clusterMap =
          new ClusterMapManager(options.hardwareLayoutFilePath, options.partitionLayoutFilePath,
              new ClusterMapConfig(verifiableProperties));
      logger.info("Bootstrapping AmberServer");
      AmberServer = new AmberServer(verifiableProperties, clusterMap, SystemTime.getInstance());
      // attach shutdown handler to catch control-c
      Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
          logger.info("Received shutdown signal. Shutting down AmberServer");
          AmberServer.shutdown();
        }
      });
      AmberServer.startup();
      AmberServer.awaitShutdown();
    } catch (Exception e) {
      logger.error("Exception during bootstrap of AmberServer", e);
      exitCode = 1;
    }
    logger.info("Exiting AmberMain");
    System.exit(exitCode);
  }
}
