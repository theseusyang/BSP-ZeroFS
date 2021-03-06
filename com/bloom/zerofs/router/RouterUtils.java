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
import com.bloom.zerofs.api.clustermap.ReplicaId;
import com.bloom.zerofs.api.config.RouterConfig;
import com.bloom.zerofs.api.router.RouterErrorCode;
import com.bloom.zerofs.api.router.RouterException;
import com.bloom.zerofs.commons.BlobId;


/**
 * This is a utility class used by Router.
 */
class RouterUtils {

  private static Logger logger = LoggerFactory.getLogger(RouterUtils.class);

  /**
   * Get {@link BlobId} from a blob string.
   * @param blobIdString The string of blobId.
   * @param clusterMap The {@link ClusterMap} based on which to generate the {@link BlobId}.
   * @return BlobId
   * @throws RouterException If parsing a string blobId fails.
   */
  static BlobId getBlobIdFromString(String blobIdString, ClusterMap clusterMap)
      throws RouterException {
    BlobId blobId;
    try {
      blobId = new BlobId(blobIdString, clusterMap);
      logger.trace("BlobId created " + blobId + " with partition " + blobId.getPartition());
    } catch (Exception e) {
      logger.error("Caller passed in invalid BlobId " + blobIdString);
      throw new RouterException("BlobId is invalid " + blobIdString, RouterErrorCode.InvalidBlobId);
    }
    return blobId;
  }

  /**
   * Checks if the given {@link ReplicaId} is in a different data center relative to a router with the
   * given {@link RouterConfig}
   * @param routerConfig the {@link RouterConfig} associated with a router
   * @param replicaId the {@link ReplicaId} whose status (local/remote) is to be determined.
   * @return true if the replica is remote, false otherwise.
   */
  static boolean isRemoteReplica(RouterConfig routerConfig, ReplicaId replicaId) {
    return !routerConfig.routerDatacenterName.equals(replicaId.getDataNodeId().getDatacenterName());
  }
}
