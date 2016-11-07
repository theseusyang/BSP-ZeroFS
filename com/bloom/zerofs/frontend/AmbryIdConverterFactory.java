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

import java.util.concurrent.Future;

import com.bloom.zerofs.api.config.VerifiableProperties;
import com.bloom.zerofs.api.rest.IdConverter;
import com.bloom.zerofs.api.rest.IdConverterFactory;
import com.bloom.zerofs.api.rest.RestMethod;
import com.bloom.zerofs.api.rest.RestRequest;
import com.bloom.zerofs.api.rest.RestServiceErrorCode;
import com.bloom.zerofs.api.rest.RestServiceException;
import com.bloom.zerofs.api.router.Callback;
import com.bloom.zerofs.api.router.FutureResult;
import com.codahale.metrics.MetricRegistry;


/**
 * Factory that instantiates an {@link IdConverter} implementation for the frontend.
 */
public class AmberIdConverterFactory implements IdConverterFactory {

  private final FrontendMetrics frontendMetrics;

  public AmberIdConverterFactory(VerifiableProperties verifiableProperties, MetricRegistry metricRegistry) {
    frontendMetrics = new FrontendMetrics(metricRegistry);
  }

  @Override
  public IdConverter getIdConverter() {
    return new AmberIdConverter(frontendMetrics);
  }

  private static class AmberIdConverter implements IdConverter {
    private boolean isOpen = true;
    private final FrontendMetrics frontendMetrics;

    AmberIdConverter(FrontendMetrics frontendMetrics) {
      this.frontendMetrics = frontendMetrics;
    }

    @Override
    public void close() {
      isOpen = false;
    }

    /**
     * {@inheritDoc}
     * On {@link RestMethod#POST}, adds a leading slash to indicate that the ID represents the path of the resource
     * created.
     * On any other {@link RestMethod}, removes the leading slash in order to convert the path into an ID that the
     * {@link com.bloom.zerofs.router.Router} will understand.
     * @param restRequest {@link RestRequest} representing the request.
     * @param input the ID that needs to be converted.
     * @param callback the {@link Callback} to invoke once the converted ID is available. Can be null.
     * @return a {@link Future} that will eventually contain the converted ID.
     */
    @Override
    public Future<String> convert(RestRequest restRequest, String input, Callback<String> callback) {
      FutureResult<String> futureResult = new FutureResult<String>();
      String convertedId = null;
      Exception exception = null;
      frontendMetrics.idConverterRequestRate.mark();
      long startTimeInMs = System.currentTimeMillis();
      if (!isOpen) {
        exception = new RestServiceException("IdConverter is closed", RestServiceErrorCode.ServiceUnavailable);
      } else if (restRequest.getRestMethod().equals(RestMethod.POST)) {
        convertedId = "/" + input;
      } else {
        convertedId = input.startsWith("/") ? input.substring(1) : input;
      }
      futureResult.done(convertedId, exception);
      if (callback != null) {
        callback.onCompletion(convertedId, exception);
      }
      frontendMetrics.idConverterProcessingTimeInMs.update(System.currentTimeMillis() - startTimeInMs);
      return futureResult;
    }
  }
}
