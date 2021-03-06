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
package com.bloom.zerofs.store;

import com.bloom.zerofs.api.store.StoreKey;

/**
 * A key and value that represents an index entry
 */
public class IndexEntry {
  private StoreKey key;
  private IndexValue value;

  public IndexEntry(StoreKey key, IndexValue value) {
    this.key = key;
    this.value = value;
  }

  public StoreKey getKey() {
    return this.key;
  }

  public IndexValue getValue() {
    return this.value;
  }
}
