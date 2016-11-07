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
package com.bloom.zerofs.api.config;


/**
 * Configuration parameters required by RestServer and Rest infrastructure
 */
public class RestServerConfig {
  /**
   * The BlobStorageServiceFactory that needs to be used by the RestServer
   * for bootstrapping the BlobStorageService.
   */
  @Config("rest.server.blob.storage.service.factory")
  public final String restServerBlobStorageServiceFactory;

  /**
   * The NioServerFactory that needs to be used by the RestServer for
   * bootstrapping the NioServer
   */
  @Config("rest.server.nio.server.factory")
  @Default("com.bloom.zerofs.rest.NettyServerFactory")
  public final String restServerNioServerFactory;

  /**
   * The number of scaling units in RestRequestHandler that will handle requests.
   */
  @Config("rest.server.request.handler.scaling.unit.count")
  @Default("5")
  public final int restServerRequestHandlerScalingUnitCount;

  /**
   * The RestRequestHandlerFactory that needs to be used by the RestServer
   * for bootstrapping the RestRequestHandler
   */
  @Config("rest.server.request.handler.factory")
  @Default("com.bloom.zerofs.rest.AsyncRequestResponseHandlerFactory")
  public final String restServerRequestHandlerFactory;

  /**
   * The number of scaling units in RestResponseHandler handle responses.
   */
  @Config("rest.server.response.handler.scaling.unit.count")
  @Default("5")
  public final int restServerResponseHandlerScalingUnitCount;

  /**
   * The RestResponseHandlerFactory that needs to be used by the RestServer
   * for bootstrapping the RestResponseHandler.
   */
  @Config("rest.server.response.handler.factory")
  @Default("com.bloom.zerofs.rest.AsyncRequestResponseHandlerFactory")
  public final String restServerResponseHandlerFactory;

  /**
   * The RouterFactory that needs to be used by the RestServer
   * for bootstrapping the Router.
   */
  @Config("rest.server.router.factory")
  @Default("com.bloom.zerofs.router.CoordinatorBackedRouterFactory")
  public final String restServerRouterFactory;

  /**
   * Request Headers that needs to be logged as part of public access log entries
   */
  @Config("rest.server.public.access.log.request.headers")
  @Default(
      "Host,Referer,User-Agent,Content-Length,x-Amber-content-type,x-Amber-owner-id,x-Amber-ttl,x-Amber-private,x-Amber-service-id,X-Forwarded-For")
  public final String restServerPublicAccessLogRequestHeaders;

  /**
   * Response Headers that needs to be logged as part of public access log entries
   */
  @Config("rest.server.public.access.log.response.headers")
  @Default("Location,x-Amber-blob-size")
  public final String restServerPublicAccessLogResponseHeaders;

  /**
   * Health check URI for load balancers (VIPs)
   */
  @Config("rest.server.health.check.uri")
  @Default("/healthCheck")
  public final String restServerHealthCheckUri;

  public RestServerConfig(VerifiableProperties verifiableProperties) {
    restServerBlobStorageServiceFactory = verifiableProperties.getString("rest.server.blob.storage.service.factory");
    restServerNioServerFactory =
        verifiableProperties.getString("rest.server.nio.server.factory", "com.bloom.zerofs.rest.NettyServerFactory");
    restServerRequestHandlerScalingUnitCount =
        verifiableProperties.getIntInRange("rest.server.request.handler.scaling.unit.count", 5, 0, Integer.MAX_VALUE);
    restServerRequestHandlerFactory = verifiableProperties
        .getString("rest.server.request.handler.factory", "com.bloom.zerofs.rest.AsyncRequestResponseHandlerFactory");
    restServerResponseHandlerScalingUnitCount =
        verifiableProperties.getIntInRange("rest.server.response.handler.scaling.unit.count", 5, 0, Integer.MAX_VALUE);
    restServerResponseHandlerFactory = verifiableProperties
        .getString("rest.server.response.handler.factory", "com.bloom.zerofs.rest.AsyncRequestResponseHandlerFactory");
    restServerRouterFactory = verifiableProperties
        .getString("rest.server.router.factory", "com.bloom.zerofs.router.CoordinatorBackedRouterFactory");
    restServerPublicAccessLogRequestHeaders = verifiableProperties
        .getString("rest.server.public.access.log.request.headers",
            "Host,Referer,User-Agent,Content-Length,x-Amber-content-type,x-Amber-owner-id,x-Amber-ttl,x-Amber-private,x-Amber-service-id,X-Forwarded-For");
    restServerPublicAccessLogResponseHeaders =
        verifiableProperties.getString("rest.server.public.access.log.response.headers", "Location,x-Amber-blob-size");
    restServerHealthCheckUri = verifiableProperties.getString("rest.server.health.check.uri", "/healthCheck");
  }
}
