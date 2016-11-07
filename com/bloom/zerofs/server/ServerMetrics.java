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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;


/**
 * Metrics for the server
 */
public class ServerMetrics {

  public static final long smallBlob = 50 * 1024; // up to and including 50KB
  public static final long mediumBlob = 1 * 1024 * 1024; // up to and including 1MB
  // largeBlob is everything larger than mediumBlob

  public final Histogram putBlobRequestQueueTimeInMs;
  public final Histogram putBlobProcessingTimeInMs;
  public final Histogram putBlobResponseQueueTimeInMs;
  public final Histogram putBlobSendTimeInMs;
  public final Histogram putBlobTotalTimeInMs;

  public final Histogram putSmallBlobProcessingTimeInMs;
  public final Histogram putSmallBlobSendTimeInMs;
  public final Histogram putSmallBlobTotalTimeInMs;

  public final Histogram putMediumBlobProcessingTimeInMs;
  public final Histogram putMediumBlobSendTimeInMs;
  public final Histogram putMediumBlobTotalTimeInMs;

  public final Histogram putLargeBlobProcessingTimeInMs;
  public final Histogram putLargeBlobSendTimeInMs;
  public final Histogram putLargeBlobTotalTimeInMs;

  public final Histogram getBlobRequestQueueTimeInMs;
  public final Histogram getBlobProcessingTimeInMs;
  public final Histogram getBlobResponseQueueTimeInMs;
  public final Histogram getBlobSendTimeInMs;
  public final Histogram getBlobTotalTimeInMs;

  public final Histogram getSmallBlobProcessingTimeInMs;
  public final Histogram getSmallBlobSendTimeInMs;
  public final Histogram getSmallBlobTotalTimeInMs;

  public final Histogram getMediumBlobProcessingTimeInMs;
  public final Histogram getMediumBlobSendTimeInMs;
  public final Histogram getMediumBlobTotalTimeInMs;

  public final Histogram getLargeBlobProcessingTimeInMs;
  public final Histogram getLargeBlobSendTimeInMs;
  public final Histogram getLargeBlobTotalTimeInMs;

  public final Histogram getBlobPropertiesRequestQueueTimeInMs;
  public final Histogram getBlobPropertiesProcessingTimeInMs;
  public final Histogram getBlobPropertiesResponseQueueTimeInMs;
  public final Histogram getBlobPropertiesSendTimeInMs;
  public final Histogram getBlobPropertiesTotalTimeInMs;

  public final Histogram getBlobUserMetadataRequestQueueTimeInMs;
  public final Histogram getBlobUserMetadataProcessingTimeInMs;
  public final Histogram getBlobUserMetadataResponseQueueTimeInMs;
  public final Histogram getBlobUserMetadataSendTimeInMs;
  public final Histogram getBlobUserMetadataTotalTimeInMs;

  public final Histogram getBlobAllRequestQueueTimeInMs;
  public final Histogram getBlobAllProcessingTimeInMs;
  public final Histogram getBlobAllResponseQueueTimeInMs;
  public final Histogram getBlobAllSendTimeInMs;
  public final Histogram getBlobAllTotalTimeInMs;

  public final Histogram getBlobInfoRequestQueueTimeInMs;
  public final Histogram getBlobInfoProcessingTimeInMs;
  public final Histogram getBlobInfoResponseQueueTimeInMs;
  public final Histogram getBlobInfoSendTimeInMs;
  public final Histogram getBlobInfoTotalTimeInMs;

  public final Histogram deleteBlobRequestQueueTimeInMs;
  public final Histogram deleteBlobProcessingTimeInMs;
  public final Histogram deleteBlobResponseQueueTimeInMs;
  public final Histogram deleteBlobSendTimeInMs;
  public final Histogram deleteBlobTotalTimeInMs;

  public final Histogram ttlBlobRequestQueueTimeInMs;
  public final Histogram ttlBlobProcessingTimeInMs;
  public final Histogram ttlBlobResponseQueueTimeInMs;
  public final Histogram ttlBlobSendTimeInMs;
  public final Histogram ttlBlobTotalTimeInMs;

  public final Histogram replicaMetadataRequestQueueTimeInMs;
  public final Histogram replicaMetadataRequestProcessingTimeInMs;
  public final Histogram replicaMetadataResponseQueueTimeInMs;
  public final Histogram replicaMetadataSendTimeInMs;
  public final Histogram replicaMetadataTotalTimeInMs;

  public final Histogram blobSizeInBytes;
  public final Histogram blobUserMetadataSizeInBytes;

  public final Histogram serverStartTimeInMs;
  public final Histogram serverShutdownTimeInMs;

  public final Meter putBlobRequestRate;
  public final Meter getBlobRequestRate;
  public final Meter getBlobPropertiesRequestRate;
  public final Meter getBlobUserMetadataRequestRate;
  public final Meter getBlobAllRequestRate;
  public final Meter getBlobInfoRequestRate;
  public final Meter deleteBlobRequestRate;
  public final Meter ttlBlobRequestRate;
  public final Meter replicaMetadataRequestRate;

  public final Meter putSmallBlobRequestRate;
  public final Meter getSmallBlobRequestRate;

  public final Meter putMediumBlobRequestRate;
  public final Meter getMediumBlobRequestRate;

  public final Meter putLargeBlobRequestRate;
  public final Meter getLargeBlobRequestRate;

  public final Counter partitionUnknownError;
  public final Counter diskUnavailableError;
  public final Counter partitionReadOnlyError;
  public final Counter storeIOError;
  public final Counter unExpectedStorePutError;
  public final Counter unExpectedStoreGetError;
  public final Counter unExpectedStoreTTLError;
  public final Counter unExpectedStoreDeleteError;
  public final Counter unExpectedStoreFindEntriesError;
  public final Counter idAlreadyExistError;
  public final Counter dataCorruptError;
  public final Counter unknownFormatError;
  public final Counter idNotFoundError;
  public final Counter idDeletedError;
  public final Counter ttlExpiredError;

  public ServerMetrics(MetricRegistry registry) {
    putBlobRequestQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "PutBlobRequestQueueTime"));
    putBlobProcessingTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "PutBlobProcessingTime"));
    putBlobResponseQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "PutBlobResponseQueueTime"));
    putBlobSendTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "PutBlobSendTime"));
    putBlobTotalTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "PutBlobTotalTime"));

    putSmallBlobProcessingTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "PutSmallBlobProcessingTime"));
    putSmallBlobSendTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "PutSmallBlobSendTime"));
    putSmallBlobTotalTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "PutSmallBlobTotalTime"));

    putMediumBlobProcessingTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "PutMediumBlobProcessingTime"));
    putMediumBlobSendTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "PutMediumBlobSendTime"));
    putMediumBlobTotalTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "PutMediumBlobTotalTime"));

    putLargeBlobProcessingTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "PutLargeBlobProcessingTime"));
    putLargeBlobSendTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "PutLargeBlobSendTime"));
    putLargeBlobTotalTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "PutLargeBlobTotalTime"));

    getBlobRequestQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobRequestQueueTime"));
    getBlobProcessingTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobProcessingTime"));
    getBlobResponseQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobResponseQueueTime"));
    getBlobSendTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobSendTime"));
    getBlobTotalTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobTotalTime"));

    getSmallBlobProcessingTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetSmallBlobProcessingTime"));
    getSmallBlobSendTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "GetSmallBlobSendTime"));
    getSmallBlobTotalTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "GetSmallBlobTotalTime"));

    getMediumBlobProcessingTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetMediumBlobProcessingTime"));
    getMediumBlobSendTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "GetMediumBlobSendTime"));
    getMediumBlobTotalTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "GetMediumBlobTotalTime"));

    getLargeBlobProcessingTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetLargeBlobProcessingTime"));
    getLargeBlobSendTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "GetLargeBlobSendTime"));
    getLargeBlobTotalTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "GetLargeBlobTotalTime"));

    getBlobPropertiesRequestQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobPropertiesRequestQueueTime"));
    getBlobPropertiesProcessingTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobPropertiesProcessingTime"));
    getBlobPropertiesResponseQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobPropertiesResponseQueueTime"));
    getBlobPropertiesSendTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobPropertiesSendTime"));
    getBlobPropertiesTotalTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobPropertiesTotalTime"));

    getBlobUserMetadataRequestQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobUserMetadataRequestQueueTime"));
    getBlobUserMetadataProcessingTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobUserMetadataProcessingTime"));
    getBlobUserMetadataResponseQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobUserMetadataResponseQueueTime"));
    getBlobUserMetadataSendTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobUserMetadataSendTime"));
    getBlobUserMetadataTotalTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobUserMetadataTotalTime"));

    getBlobAllRequestQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobAllRequestQueueTime"));
    getBlobAllProcessingTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobAllProcessingTime"));
    getBlobAllResponseQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobAllResponseQueueTime"));
    getBlobAllSendTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobAllSendTime"));
    getBlobAllTotalTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobAllTotalTime"));

    getBlobInfoRequestQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobInfoRequestQueueTime"));
    getBlobInfoProcessingTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobInfoProcessingTime"));
    getBlobInfoResponseQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobInfoResponseQueueTime"));
    getBlobInfoSendTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobInfoSendTime"));
    getBlobInfoTotalTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "GetBlobInfoTotalTime"));

    deleteBlobRequestQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "DeleteBlobRequestQueueTime"));
    deleteBlobProcessingTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "DeleteBlobProcessingTime"));
    deleteBlobResponseQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "DeleteBlobResponseQueueTime"));
    deleteBlobSendTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "DeleteBlobSendTime"));
    deleteBlobTotalTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "DeleteBlobTotalTime"));

    ttlBlobRequestQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "TTLBlobRequestQueueTime"));
    ttlBlobProcessingTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "TTLBlobProcessingTime"));
    ttlBlobResponseQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "TTLBlobResponseQueueTime"));
    ttlBlobSendTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "TTLBlobSendTime"));
    ttlBlobTotalTimeInMs = registry.histogram(MetricRegistry.name(AmberRequests.class, "TTLBlobTotalTime"));

    replicaMetadataRequestQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "ReplicaMetadataRequestQueueTime"));
    replicaMetadataRequestProcessingTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "ReplicaMetadataRequestProcessingTime"));
    replicaMetadataResponseQueueTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "ReplicaMetadataResponseQueueTime"));
    replicaMetadataSendTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "ReplicaMetadataSendTime"));
    replicaMetadataTotalTimeInMs =
        registry.histogram(MetricRegistry.name(AmberRequests.class, "ReplicaMetadataTotalTime"));

    blobSizeInBytes = registry.histogram(MetricRegistry.name(AmberRequests.class, "BlobSize"));
    blobUserMetadataSizeInBytes = registry.histogram(MetricRegistry.name(AmberRequests.class, "BlobUserMetadataSize"));

    serverStartTimeInMs = registry.histogram(MetricRegistry.name(AmberServer.class, "ServerStartTimeInMs"));
    serverShutdownTimeInMs = registry.histogram(MetricRegistry.name(AmberServer.class, "ServerShutdownTimeInMs"));

    putBlobRequestRate = registry.meter(MetricRegistry.name(AmberRequests.class, "PutBlobRequestRate"));
    getBlobRequestRate = registry.meter(MetricRegistry.name(AmberRequests.class, "GetBlobRequestRate"));
    getBlobPropertiesRequestRate =
        registry.meter(MetricRegistry.name(AmberRequests.class, "GetBlobPropertiesRequestRate"));
    getBlobUserMetadataRequestRate =
        registry.meter(MetricRegistry.name(AmberRequests.class, "GetBlobUserMetadataRequestRate"));
    getBlobAllRequestRate = registry.meter(MetricRegistry.name(AmberRequests.class, "GetBlobAllRequestRate"));
    getBlobInfoRequestRate = registry.meter(MetricRegistry.name(AmberRequests.class, "GetBlobInfoRequestRate"));
    deleteBlobRequestRate = registry.meter(MetricRegistry.name(AmberRequests.class, "DeleteBlobRequestRate"));
    ttlBlobRequestRate = registry.meter(MetricRegistry.name(AmberRequests.class, "TTLBlobRequestRate"));
    replicaMetadataRequestRate = registry.meter(MetricRegistry.name(AmberRequests.class, "ReplicaMetadataRequestRate"));

    putSmallBlobRequestRate = registry.meter(MetricRegistry.name(AmberRequests.class, "PutSmallBlobRequestRate"));
    getSmallBlobRequestRate = registry.meter(MetricRegistry.name(AmberRequests.class, "GetSmallBlobRequestRate"));

    putMediumBlobRequestRate = registry.meter(MetricRegistry.name(AmberRequests.class, "PutMediumBlobRequestRate"));
    getMediumBlobRequestRate = registry.meter(MetricRegistry.name(AmberRequests.class, "GetMediumBlobRequestRate"));

    putLargeBlobRequestRate = registry.meter(MetricRegistry.name(AmberRequests.class, "PutLargeBlobRequestRate"));
    getLargeBlobRequestRate = registry.meter(MetricRegistry.name(AmberRequests.class, "GetLargeBlobRequestRate"));

    partitionUnknownError = registry.counter(MetricRegistry.name(AmberRequests.class, "PartitionUnknownError"));
    diskUnavailableError = registry.counter(MetricRegistry.name(AmberRequests.class, "DiskUnavailableError"));
    partitionReadOnlyError = registry.counter(MetricRegistry.name(AmberRequests.class, "PartitionReadOnlyError"));
    storeIOError = registry.counter(MetricRegistry.name(AmberRequests.class, "StoreIOError"));
    idAlreadyExistError = registry.counter(MetricRegistry.name(AmberRequests.class, "IDAlreadyExistError"));
    dataCorruptError = registry.counter(MetricRegistry.name(AmberRequests.class, "DataCorruptError"));
    unknownFormatError = registry.counter(MetricRegistry.name(AmberRequests.class, "UnknownFormatError"));
    idNotFoundError = registry.counter(MetricRegistry.name(AmberRequests.class, "IDNotFoundError"));
    idDeletedError = registry.counter(MetricRegistry.name(AmberRequests.class, "IDDeletedError"));
    ttlExpiredError = registry.counter(MetricRegistry.name(AmberRequests.class, "TTLExpiredError"));
    unExpectedStorePutError = registry.counter(MetricRegistry.name(AmberRequests.class, "UnexpectedStorePutError"));
    unExpectedStoreGetError = registry.counter(MetricRegistry.name(AmberRequests.class, "UnexpectedStoreGetError"));
    unExpectedStoreDeleteError =
        registry.counter(MetricRegistry.name(AmberRequests.class, "UnexpectedStoreDeleteError"));
    unExpectedStoreTTLError = registry.counter(MetricRegistry.name(AmberRequests.class, "UnexpectedStoreTTLError"));
    unExpectedStoreFindEntriesError =
        registry.counter(MetricRegistry.name(AmberRequests.class, "UnexpectedStoreFindEntriesError"));
  }

  public void markPutBlobRequestRateBySize(long blobSize) {
    if (blobSize <= smallBlob) {
      putSmallBlobRequestRate.mark();
    } else if (blobSize <= mediumBlob) {
      putMediumBlobRequestRate.mark();
    } else {
      putLargeBlobRequestRate.mark();
    }
  }

  public void markGetBlobRequestRateBySize(long blobSize) {
    if (blobSize <= smallBlob) {
      getSmallBlobRequestRate.mark();
    } else if (blobSize <= mediumBlob) {
      getMediumBlobRequestRate.mark();
    } else {
      getLargeBlobRequestRate.mark();
    }
  }
}
