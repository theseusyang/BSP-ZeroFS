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
package com.bloom.zerofs.admin;

import com.codahale.metrics.MetricRegistry;
import com.bloom.zerofs.api.config.AdminConfig;
import com.bloom.zerofs.api.config.VerifiableProperties;
import com.bloom.zerofs.api.rest.SecurityService;
import com.bloom.zerofs.api.rest.SecurityServiceFactory;


/**
 * Default implementation of {@link SecurityServiceFactory} for Admin.
 * <p/>
 * Returns a new instance of {@link AdminSecurityService} on {@link #getSecurityService()} call.
 */
public class AdminSecurityServiceFactory implements SecurityServiceFactory {
  private final AdminConfig adminConfig;
  private final AdminMetrics adminMetrics;

  public AdminSecurityServiceFactory(VerifiableProperties verifiableProperties, MetricRegistry metricRegistry) {
    adminConfig = new AdminConfig(verifiableProperties);
    adminMetrics = new AdminMetrics(metricRegistry);
  }

  @Override
  public SecurityService getSecurityService()
      throws InstantiationException {
    return new AdminSecurityService(adminConfig, adminMetrics);
  }
}
