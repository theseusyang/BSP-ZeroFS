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
package com.bloom.zerofs.api.metrics;

/**
 * A metrics histogram interface
 */
public interface MetricsHistogram {
  /**
   * Updates the histogram with the given value
   * @param value The value that the histogram needs to be updated with
   */
  void update(long value);

  /**
   * Updates the histogram with the given value
   * @param value The value that the histogram needs to be updated with
   */
  void update(int value);
}
