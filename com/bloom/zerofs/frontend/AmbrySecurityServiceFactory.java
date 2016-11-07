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

import com.bloom.zerofs.api.config.FrontendConfig;
import com.bloom.zerofs.api.config.VerifiableProperties;
import com.bloom.zerofs.api.rest.SecurityService;
import com.bloom.zerofs.api.rest.SecurityServiceFactory;
import com.codahale.metrics.MetricRegistry;


/**
 * Default implementation of {@link SecurityServiceFactory} for Amber
 * <p/>
 * Returns a new instance of {@link AmberSecurityService} on {@link #getSecurityService()} call.
 */
public class AmberSecurityServiceFactory implements SecurityServiceFactory {

  private final FrontendConfig frontendConfig;
  private final FrontendMetrics frontendMetrics;

  public AmberSecurityServiceFactory(VerifiableProperties verifiableProperties, MetricRegistry metricRegistry) {
    frontendConfig = new FrontendConfig(verifiableProperties);
    frontendMetrics = new FrontendMetrics(metricRegistry);
  }

  @Override
  public SecurityService getSecurityService()
      throws InstantiationException {
    return new AmberSecurityService(frontendConfig, frontendMetrics);
  }
}
