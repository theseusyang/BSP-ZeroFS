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
package com.bloom.zerofs.frontend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloom.zerofs.api.clustermap.ClusterMap;
import com.bloom.zerofs.api.config.FrontendConfig;
import com.bloom.zerofs.api.config.VerifiableProperties;
import com.bloom.zerofs.api.rest.BlobStorageService;
import com.bloom.zerofs.api.rest.BlobStorageServiceFactory;
import com.bloom.zerofs.api.rest.IdConverterFactory;
import com.bloom.zerofs.api.rest.RestResponseHandler;
import com.bloom.zerofs.api.rest.SecurityServiceFactory;
import com.bloom.zerofs.api.router.Router;
import com.bloom.zerofs.tools.Utils;


/**
 * Amber frontend specific implementation of {@link BlobStorageServiceFactory}.
 * <p/>
 * Sets up all the supporting cast required for the operation of {@link AmberBlobStorageService} and returns a new
 * instance on {@link #getBlobStorageService()}.
 */
public class AmberBlobStorageServiceFactory implements BlobStorageServiceFactory {
  private final FrontendConfig frontendConfig;
  private final FrontendMetrics frontendMetrics;
  private final ClusterMap clusterMap;
  private final RestResponseHandler responseHandler;
  private final Router router;
  private final IdConverterFactory idConverterFactory;
  private final SecurityServiceFactory securityServiceFactory;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Creates a new instance of AmberBlobStorageServiceFactory.
   * @param verifiableProperties the properties to use to create configs.
   * @param clusterMap the {@link ClusterMap} to use.
   * @param responseHandler the {@link RestResponseHandler} that can be used to submit responses that need to be sent
   *                        out.
   * @param router the {@link Router} to use.
   * @throws IllegalArgumentException if any of the arguments are null.
   */
  public AmberBlobStorageServiceFactory(VerifiableProperties verifiableProperties, ClusterMap clusterMap,
      RestResponseHandler responseHandler, Router router)
      throws Exception {
    if (verifiableProperties == null || clusterMap == null || responseHandler == null || router == null) {
      throw new IllegalArgumentException("Null arguments were provided during instantiation!");
    } else {
      frontendConfig = new FrontendConfig(verifiableProperties);
      frontendMetrics = new FrontendMetrics(clusterMap.getMetricRegistry());
      this.clusterMap = clusterMap;
      this.responseHandler = responseHandler;
      this.router = router;
      idConverterFactory =
          Utils.getObj(frontendConfig.frontendIdConverterFactory, verifiableProperties, clusterMap.getMetricRegistry());
      securityServiceFactory = Utils
          .getObj(frontendConfig.frontendSecurityServiceFactory, verifiableProperties, clusterMap.getMetricRegistry());
    }
    logger.trace("Instantiated AmberBlobStorageServiceFactory");
  }

  /**
   * Returns a new instance of {@link AmberBlobStorageService}.
   * @return a new instance of {@link AmberBlobStorageService}.
   */
  @Override
  public BlobStorageService getBlobStorageService() {
    return new AmberBlobStorageService(frontendConfig, frontendMetrics, responseHandler, router, idConverterFactory,
        securityServiceFactory);
  }
}
