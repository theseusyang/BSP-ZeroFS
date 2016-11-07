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
package com.bloom.zerofs.commons;

import java.io.IOException;
import java.net.SocketException;

import com.bloom.zerofs.api.clustermap.ClusterMap;
import com.bloom.zerofs.api.clustermap.ReplicaEventType;
import com.bloom.zerofs.api.clustermap.ReplicaId;
import com.bloom.zerofs.api.network.ConnectionPoolTimeoutException;


/**
 * ResponseHandler can be used by components whenever an operation encounters an error or an exception, to delegate
 * the responsibility of conveying appropriate replica related errors to the cluster map.
 * It can also be used to convey the information that a replica related operation was successful.
 * The cluster map uses this information to set soft states and dynamically handle failures.
 */

public class ResponseHandler {

  private ClusterMap clusterMap;

  public ResponseHandler(ClusterMap clusterMap) {
    this.clusterMap = clusterMap;
  }

  public void onRequestResponseError(ReplicaId replicaId, ServerErrorCode errorCode) {
    switch (errorCode) {
      case IO_Error:
      case Disk_Unavailable:
        clusterMap.onReplicaEvent(replicaId, ReplicaEventType.Disk_Error);
        break;
      case Partition_ReadOnly:
        clusterMap.onReplicaEvent(replicaId, ReplicaEventType.Partition_ReadOnly);
        //fall through
      default:
        clusterMap.onReplicaEvent(replicaId, ReplicaEventType.Disk_Ok);
        break;
    }
    // Regardless of what the error code is (or there is no error), it is a node response event.
    clusterMap.onReplicaEvent(replicaId, ReplicaEventType.Node_Response);
  }

  public void onRequestResponseException(ReplicaId replicaId, Exception e) {
    if (e instanceof SocketException ||
        e instanceof IOException ||
        e instanceof ConnectionPoolTimeoutException) {
      clusterMap.onReplicaEvent(replicaId, ReplicaEventType.Node_Timeout);
    }
  }
}
