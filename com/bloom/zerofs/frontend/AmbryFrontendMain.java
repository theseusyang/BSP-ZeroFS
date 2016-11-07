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

import com.bloom.zerofs.rest.RestServerMain;


/**
 * Used for starting/stopping an instance of {@link com.bloom.zerofs.rest.RestServer} that acts as an Amber frontend.
 */
public class AmberFrontendMain {

  public static void main(String[] args) {
    RestServerMain.main(args);
  }
}
