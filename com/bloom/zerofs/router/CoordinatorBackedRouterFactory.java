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
package com.bloom.zerofs.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloom.zerofs.api.clustermap.ClusterMap;
import com.bloom.zerofs.api.config.RouterConfig;
import com.bloom.zerofs.api.config.VerifiableProperties;
import com.bloom.zerofs.api.coordinator.Coordinator;
import com.bloom.zerofs.api.notification.NotificationSystem;
import com.bloom.zerofs.api.router.Router;
import com.bloom.zerofs.api.router.RouterFactory;
import com.bloom.zerofs.coordinator.AmberCoordinator;


/**
 * {@link CoordinatorBackedRouter} specific implementation of {@link RouterFactory}.
 * <p/>
 * Sets up all the supporting cast required for the operation of {@link CoordinatorBackedRouter} and returns a new
 * instance on {@link #getRouter()}.
 */
public class CoordinatorBackedRouterFactory implements RouterFactory {
  private final RouterConfig routerConfig;
  private final CoordinatorBackedRouterMetrics coordinatorBackedRouterMetrics;
  private final Coordinator coordinator;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Creates an instance of CoordinatorBackedRouterFactory with the given {@code verifiableProperties},
   * {@code clusterMap} and {@code notificationSystem}.
   * @param verifiableProperties the in-memory properties to use to construct configurations.
   * @param clusterMap the {@link ClusterMap} to use to determine where operations should go.
   * @param notificationSystem the {@link NotificationSystem} to use to log operations.
   * @throws IllegalArgumentException if any of the arguments are null.
   */
  public CoordinatorBackedRouterFactory(VerifiableProperties verifiableProperties, ClusterMap clusterMap,
      NotificationSystem notificationSystem) {
    if (verifiableProperties == null || clusterMap == null || notificationSystem == null) {
      throw new IllegalArgumentException("Null arg(s) received during instantiation of CoordinatorBackedRouterFactory");
    } else {
      routerConfig = new RouterConfig(verifiableProperties);
      coordinatorBackedRouterMetrics = new CoordinatorBackedRouterMetrics(clusterMap.getMetricRegistry());
      coordinator = new AmberCoordinator(verifiableProperties, clusterMap, notificationSystem);
      logger.trace("Instantiated CoordinatorBackedRouterFactory");
    }
  }

  @Override
  public Router getRouter() {
    return new CoordinatorBackedRouter(routerConfig, coordinatorBackedRouterMetrics, coordinator);
  }
}
