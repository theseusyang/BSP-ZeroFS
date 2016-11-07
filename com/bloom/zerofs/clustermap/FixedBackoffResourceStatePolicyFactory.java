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
package com.bloom.zerofs.clustermap;

import com.bloom.zerofs.api.clustermap.DataNodeId;
import com.bloom.zerofs.api.clustermap.DiskId;
import com.bloom.zerofs.api.clustermap.HardwareState;
import com.bloom.zerofs.api.clustermap.Resource;
import com.bloom.zerofs.api.clustermap.ResourceStatePolicy;
import com.bloom.zerofs.api.clustermap.ResourceStatePolicyFactory;
import com.bloom.zerofs.api.config.ClusterMapConfig;


public class FixedBackoffResourceStatePolicyFactory implements ResourceStatePolicyFactory {
  private ResourceStatePolicy resourceStatePolicy;

  public FixedBackoffResourceStatePolicyFactory(Resource resource, HardwareState initialState,
      ClusterMapConfig clusterMapConfig)
      throws InstantiationError {
    resourceStatePolicy = null;
    if (resource instanceof DataNodeId) {
      resourceStatePolicy = new FixedBackoffResourceStatePolicy(resource, initialState == HardwareState.UNAVAILABLE,
          clusterMapConfig.clusterMapFixedTimeoutDatanodeErrorThreshold,
          clusterMapConfig.clusterMapFixedTimeoutDataNodeRetryBackoffMs);
    } else if (resource instanceof DiskId) {
      resourceStatePolicy = new FixedBackoffResourceStatePolicy(resource, initialState == HardwareState.UNAVAILABLE,
          clusterMapConfig.clusterMapFixedTimeoutDiskErrorThreshold,
          clusterMapConfig.clusterMapFixedTimeoutDiskRetryBackoffMs);
    }

    if (resourceStatePolicy == null) {
      throw new InstantiationError("Unknown resource type, cannot get resource state policy.");
    }
  }

  @Override
  public ResourceStatePolicy getResourceStatePolicy() {
    return resourceStatePolicy;
  }
}
