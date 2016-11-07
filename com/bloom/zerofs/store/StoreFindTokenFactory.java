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

import java.io.DataInputStream;
import java.io.IOException;

import com.bloom.zerofs.api.store.FindToken;
import com.bloom.zerofs.api.store.FindTokenFactory;
import com.bloom.zerofs.api.store.StoreKeyFactory;


/**
 * Factory that creates the store token from an inputstream
 */
public class StoreFindTokenFactory implements FindTokenFactory {
  private StoreKeyFactory factory;

  public StoreFindTokenFactory(StoreKeyFactory factory) {
    this.factory = factory;
  }

  @Override
  public FindToken getFindToken(DataInputStream stream)
      throws IOException {
    return StoreFindToken.fromBytes(stream, factory);
  }

  @Override
  public FindToken getNewFindToken() {
    return new StoreFindToken();
  }
}
