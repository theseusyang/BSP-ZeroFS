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

import java.io.DataInputStream;
import java.io.IOException;

import com.bloom.zerofs.api.clustermap.ClusterMap;
import com.bloom.zerofs.api.store.StoreKey;
import com.bloom.zerofs.api.store.StoreKeyFactory;


/**
 * The BlobId factory that creates the blobId
 */
public class BlobIdFactory implements StoreKeyFactory {

  private ClusterMap clusterMap;

  public BlobIdFactory(ClusterMap clusterMap) {
    this.clusterMap = clusterMap;
  }

  @Override
  public StoreKey getStoreKey(DataInputStream value)
      throws IOException {
    return new BlobId(value, clusterMap);
  }
}