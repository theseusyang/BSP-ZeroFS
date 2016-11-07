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
package com.bloom.zerofs.rest;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloom.zerofs.api.clustermap.ClusterMap;
import com.bloom.zerofs.api.config.ClusterMapConfig;
import com.bloom.zerofs.api.config.VerifiableProperties;
import com.bloom.zerofs.clustermap.ClusterMapManager;
import com.bloom.zerofs.commons.LoggingNotificationSystem;
import com.bloom.zerofs.tools.InvocationOptions;
import com.bloom.zerofs.tools.Utils;


/**
 * Start point for creating an instance of {@link RestServer} and starting/shutting it down.
 */
public class RestServerMain {
  private static Logger logger = LoggerFactory.getLogger(RestServerMain.class);

  public static void main(String[] args) {
    final RestServer restServer;
    int exitCode = 0;
    try {
      final InvocationOptions options = new InvocationOptions(args);
      final Properties properties = Utils.loadProps(options.serverPropsFilePath);
      final VerifiableProperties verifiableProperties = new VerifiableProperties(properties);
      final ClusterMap clusterMap =
          new ClusterMapManager(options.hardwareLayoutFilePath, options.partitionLayoutFilePath,
              new ClusterMapConfig(verifiableProperties));
      logger.info("Bootstrapping RestServer");
      restServer = new RestServer(verifiableProperties, clusterMap, new LoggingNotificationSystem());
      // attach shutdown handler to catch control-c
      Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
          logger.info("Received shutdown signal. Shutting down RestServer");
          restServer.shutdown();
        }
      });
      restServer.start();
      restServer.awaitShutdown();
    } catch (Exception e) {
      logger.error("Exception during bootstrap of RestServer", e);
      exitCode = 1;
    }
    logger.info("Exiting RestServerMain");
    System.exit(exitCode);
  }
}


