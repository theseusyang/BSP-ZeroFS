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

import com.bloom.zerofs.api.rest.RestRequestMetrics;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;


/**
 * Amber frontend specific metrics tracking.
 * <p/>
 * Exports metrics that are triggered by the Amber frontend to the provided {@link MetricRegistry}.
 */
class FrontendMetrics {

  // RestRequestMetrics instances
  // DELETE
  public final RestRequestMetrics deleteBlobMetrics;
  // HEAD
  public final RestRequestMetrics headBlobMetrics;
  // GET
  public final RestRequestMetrics getBlobInfoMetrics;
  public final RestRequestMetrics getBlobMetrics;
  public final RestRequestMetrics getUserMetadataMetrics;
  // POST
  public final RestRequestMetrics postBlobMetrics;

  // Rates
  // AmberSecurityService
  public final Meter securityServiceProcessRequestRate;
  public final Meter securityServiceProcessResponseRate;
  // AmberIdConverter
  public final Meter idConverterRequestRate;

  // Latencies
  // AmberBlobStorageService
  // DELETE
  public final Histogram deletePreProcessingTimeInMs;
  // HEAD
  public final Histogram headPreProcessingTimeInMs;
  // GET
  public final Histogram getPreProcessingTimeInMs;
  // POST
  public final Histogram blobPropsBuildTimeInMs;
  public final Histogram postPreProcessingTimeInMs;
  // DeleteCallback
  public final Histogram deleteCallbackProcessingTimeInMs;
  public final Histogram deleteTimeInMs;
  // HeadCallback
  public final Histogram headCallbackProcessingTimeInMs;
  public final Histogram headTimeInMs;
  public final Histogram headSecurityResponseTimeInMs;
  public final Histogram headSecurityResponseCallbackProcessingTimeInMs;
  // HeadForGetCallback
  public final Histogram headForGetCallbackProcessingTimeInMs;
  public final Histogram headForGetTimeInMs;
  public final Histogram getSecurityResponseCallbackProcessingTimeInMs;
  public final Histogram getSecurityResponseTimeInMs;
  // GetCallback
  public final Histogram getCallbackProcessingTimeInMs;
  public final Histogram getTimeInMs;
  // PostCallback
  public final Histogram outboundIdConversionCallbackProcessingTimeInMs;
  public final Histogram outboundIdConversionTimeInMs;
  public final Histogram postCallbackProcessingTimeInMs;
  public final Histogram postTimeInMs;
  public final Histogram postSecurityResponseTimeInMs;
  public final Histogram postSecurityResponseCallbackProcessingTimeInMs;
  // InboundIdConverterCallback
  public final Histogram inboundIdConversionCallbackProcessingTimeInMs;
  public final Histogram inboundIdConversionTimeInMs;
  // SecurityProcessRequestCallback
  public final Histogram deleteSecurityRequestCallbackProcessingTimeInMs;
  public final Histogram getSecurityRequestCallbackProcessingTimeInMs;
  public final Histogram headSecurityRequestCallbackProcessingTimeInMs;
  public final Histogram postSecurityRequestCallbackProcessingTimeInMs;
  public final Histogram deleteSecurityRequestTimeInMs;
  public final Histogram getSecurityRequestTimeInMs;
  public final Histogram headSecurityRequestTimeInMs;
  public final Histogram postSecurityRequestTimeInMs;
  // AmberSecurityService
  public final Histogram securityServiceProcessRequestTimeInMs;
  public final Histogram securityServiceProcessResponseTimeInMs;
  // AmberIdConverter
  public final Histogram idConverterProcessingTimeInMs;

  // Errors
  // AmberBlobStorageService
  public final Counter responseSubmissionError;
  public final Counter resourceReleaseError;
  public final Counter routerCallbackError;
  // DeleteCallback
  public final Counter deleteCallbackProcessingError;
  // HeadCallback
  public final Counter headCallbackProcessingError;
  // HeadForGetCallback
  public final Counter headForGetCallbackProcessingError;
  public final Counter getSecurityResponseCallbackProcessingError;
  // GetCallback
  public final Counter getCallbackProcessingError;
  // PostCallback
  public final Counter postCallbackProcessingError;
  public final Counter outboundIdConversionCallbackProcessingError;

  // Other
  // AmberBlobStorageService
  public final Histogram blobStorageServiceStartupTimeInMs;
  public final Histogram blobStorageServiceShutdownTimeInMs;

  /**
   * Creates an instance of FrontendMetrics using the given {@code metricRegistry}.
   * @param metricRegistry the {@link MetricRegistry} to use for the metrics.
   */
  public FrontendMetrics(MetricRegistry metricRegistry) {
    // RestRequestMetrics instances
    // DELETE
    deleteBlobMetrics = new RestRequestMetrics(AmberBlobStorageService.class, "DeleteBlob", metricRegistry);
    // HEAD
    headBlobMetrics = new RestRequestMetrics(AmberBlobStorageService.class, "HeadBlob", metricRegistry);
    // GET
    getBlobInfoMetrics = new RestRequestMetrics(AmberBlobStorageService.class, "GetBlobInfo", metricRegistry);
    getBlobMetrics = new RestRequestMetrics(AmberBlobStorageService.class, "GetBlob", metricRegistry);
    getUserMetadataMetrics = new RestRequestMetrics(AmberBlobStorageService.class, "GetUserMetadata", metricRegistry);
    // POST
    postBlobMetrics = new RestRequestMetrics(AmberBlobStorageService.class, "PostBlob", metricRegistry);

    // Rates
    // AmberSecurityService
    securityServiceProcessRequestRate =
        metricRegistry.meter(MetricRegistry.name(AmberSecurityService.class, "ProcessRequestRate"));
    securityServiceProcessResponseRate =
        metricRegistry.meter(MetricRegistry.name(AmberSecurityService.class, "ProcessResponseRate"));
    // AmberIdConverter
    idConverterRequestRate = metricRegistry.meter(MetricRegistry.name(AmberIdConverterFactory.class, "RequestRate"));

    // Latencies
    // AmberBlobStorageService
    // DELETE
    deletePreProcessingTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "DeletePreProcessingTimeInMs"));
    // HEAD
    headPreProcessingTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "HeadPreProcessingTimeInMs"));
    // GET
    getPreProcessingTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "GetPreProcessingTimeInMs"));
    // POST
    blobPropsBuildTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "BlobPropsBuildTimeInMs"));
    postPreProcessingTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "PostPreProcessingTimeInMs"));
    // DeleteCallback
    deleteCallbackProcessingTimeInMs = metricRegistry
        .histogram(MetricRegistry.name(AmberBlobStorageService.class, "DeleteCallbackProcessingTimeInMs"));
    deleteTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "DeleteCallbackResultTimeInMs"));
    // HeadCallback
    headCallbackProcessingTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "HeadCallbackProcessingTimeInMs"));
    headTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "HeadCallbackResultTimeInMs"));
    headSecurityResponseCallbackProcessingTimeInMs = metricRegistry.histogram(
        MetricRegistry.name(AmberBlobStorageService.class, "HeadSecurityResponseCallbackProcessingTimeInMs"));
    headSecurityResponseTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "HeadSecurityResponseTimeInMs"));
    // HeadForGetCallback
    headForGetCallbackProcessingTimeInMs = metricRegistry
        .histogram(MetricRegistry.name(AmberBlobStorageService.class, "HeadForGetCallbackProcessingTimeInMs"));
    headForGetTimeInMs = metricRegistry
        .histogram(MetricRegistry.name(AmberBlobStorageService.class, "HeadForGetCallbackResultTimeInMs"));
    getSecurityResponseCallbackProcessingTimeInMs = metricRegistry
        .histogram(MetricRegistry.name(AmberBlobStorageService.class, "GetSecurityResponseCallbackProcessingTimeInMs"));
    getSecurityResponseTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "GetSecurityResponseTimeInMs"));
    // GetCallback
    getCallbackProcessingTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "GetCallbackProcessingTimeInMs"));
    getTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "GetCallbackResultTimeInMs"));
    // PostCallback
    outboundIdConversionCallbackProcessingTimeInMs = metricRegistry
        .histogram(MetricRegistry.name(AmberBlobStorageService.class, "OutboundIdCallbackProcessingTimeInMs"));
    outboundIdConversionTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "OutboundIdConversionTimeInMs"));
    postCallbackProcessingTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "PostCallbackProcessingTimeInMs"));
    postTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "PostCallbackResultTimeInMs"));
    postSecurityResponseTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "PostSecurityResponseTimeInMs"));
    postSecurityResponseCallbackProcessingTimeInMs = metricRegistry.histogram(
        MetricRegistry.name(AmberBlobStorageService.class, "PostSecurityResponseCallbackProcessingTimeInMs"));
    // InboundIdConverterCallback
    inboundIdConversionCallbackProcessingTimeInMs = metricRegistry
        .histogram(MetricRegistry.name(AmberBlobStorageService.class, "InboundIdCallbackProcessingTimeInMs"));
    inboundIdConversionTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "InboundIdConversionTimeInMs"));
    // SecurityProcessRequestCallback
    deleteSecurityRequestCallbackProcessingTimeInMs = metricRegistry.histogram(
        MetricRegistry.name(AmberBlobStorageService.class, "DeleteSecurityRequestCallbackProcessingTimeInMs"));
    deleteSecurityRequestTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "DeleteSecurityRequestTimeInMs"));
    headSecurityRequestCallbackProcessingTimeInMs = metricRegistry
        .histogram(MetricRegistry.name(AmberBlobStorageService.class, "HeadSecurityRequestCallbackProcessingTimeInMs"));
    headSecurityRequestTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "HeadSecurityRequestTimeInMs"));
    getSecurityRequestCallbackProcessingTimeInMs = metricRegistry
        .histogram(MetricRegistry.name(AmberBlobStorageService.class, "GetSecurityRequestCallbackProcessingTimeInMs"));
    getSecurityRequestTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "GetSecurityRequestTimeInMs"));
    postSecurityRequestCallbackProcessingTimeInMs = metricRegistry
        .histogram(MetricRegistry.name(AmberBlobStorageService.class, "PostSecurityRequestCallbackProcessingTimeInMs"));
    postSecurityRequestTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "PostSecurityRequestTimeInMs"));
    // AmberSecurityService
    securityServiceProcessRequestTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberSecurityService.class, "RequestProcessingTimeInMs"));
    securityServiceProcessResponseTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberSecurityService.class, "ResponseProcessingTimeInMs"));
    // AmberIdConverter
    idConverterProcessingTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberIdConverterFactory.class, "ProcessingTimeInMs"));

    // Errors
    // AmberBlobStorageService
    responseSubmissionError =
        metricRegistry.counter(MetricRegistry.name(AmberBlobStorageService.class, "ResponseSubmissionError"));
    resourceReleaseError =
        metricRegistry.counter(MetricRegistry.name(AmberBlobStorageService.class, "ResourceReleaseError"));
    routerCallbackError =
        metricRegistry.counter(MetricRegistry.name(AmberBlobStorageService.class, "RouterCallbackError"));
    // DeleteCallback
    deleteCallbackProcessingError =
        metricRegistry.counter(MetricRegistry.name(AmberBlobStorageService.class, "DeleteCallbackProcessingError"));
    // HeadCallback
    headCallbackProcessingError =
        metricRegistry.counter(MetricRegistry.name(AmberBlobStorageService.class, "HeadCallbackProcessingError"));
    // HeadForGetCallback
    headForGetCallbackProcessingError =
        metricRegistry.counter(MetricRegistry.name(AmberBlobStorageService.class, "HeadForGetCallbackProcessingError"));
    getSecurityResponseCallbackProcessingError = metricRegistry
        .counter(MetricRegistry.name(AmberBlobStorageService.class, "GetSecurityResponseCallbackProcessingError"));
    // GetCallback
    getCallbackProcessingError =
        metricRegistry.counter(MetricRegistry.name(AmberBlobStorageService.class, "GetCallbackProcessingError"));
    // PostCallback
    postCallbackProcessingError =
        metricRegistry.counter(MetricRegistry.name(AmberBlobStorageService.class, "PostCallbackProcessingError"));
    outboundIdConversionCallbackProcessingError = metricRegistry
        .counter(MetricRegistry.name(AmberBlobStorageService.class, "OutboundIdConversionCallbackProcessingError"));

    // Other
    blobStorageServiceStartupTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "StartupTimeInMs"));
    blobStorageServiceShutdownTimeInMs =
        metricRegistry.histogram(MetricRegistry.name(AmberBlobStorageService.class, "ShutdownTimeInMs"));
  }
}
