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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloom.zerofs.api.clustermap.ClusterMap;
import com.bloom.zerofs.api.config.RouterConfig;
import com.bloom.zerofs.api.notification.NotificationSystem;
import com.bloom.zerofs.api.router.Callback;
import com.bloom.zerofs.api.router.FutureResult;
import com.bloom.zerofs.api.router.RouterErrorCode;
import com.bloom.zerofs.api.router.RouterException;
import com.bloom.zerofs.commons.BlobId;
import com.bloom.zerofs.commons.ResponseHandler;
import com.bloom.zerofs.network.RequestInfo;
import com.bloom.zerofs.network.ResponseInfo;
import com.bloom.zerofs.protocol.DeleteRequest;
import com.bloom.zerofs.protocol.RequestOrResponse;
import com.bloom.zerofs.tools.Time;


/**
 * Handles {@link DeleteOperation}. A {@code DeleteManager} keeps track of all the delete
 * operations that are assigned to it, and manages their states and life cycles.
 */
class DeleteManager {
  private final Set<DeleteOperation> deleteOperations;
  private final HashMap<Integer, DeleteOperation> correlationIdToDeleteOperation;
  private final NotificationSystem notificationSystem;
  private final Time time;
  private final ResponseHandler responseHandler;
  private final NonBlockingRouterMetrics routerMetrics;
  private final ClusterMap clusterMap;
  private final RouterConfig routerConfig;
  private final OperationCompleteCallback operationCompleteCallback;

  private static final Logger logger = LoggerFactory.getLogger(DeleteManager.class);

  /**
   * Used by a {@link DeleteOperation} to associate a {@code CorrelationId} to a {@link DeleteOperation}.
   */
  private class DeleteRequestRegistrationCallbackImpl implements RequestRegistrationCallback<DeleteOperation> {
    private List<RequestInfo> requestListToFill;

    @Override
    public void registerRequestToSend(DeleteOperation deleteOperation, RequestInfo requestInfo) {
      requestListToFill.add(requestInfo);
      correlationIdToDeleteOperation
          .put(((RequestOrResponse) requestInfo.getRequest()).getCorrelationId(), deleteOperation);
    }
  }

  private final DeleteRequestRegistrationCallbackImpl requestRegistrationCallback =
      new DeleteRequestRegistrationCallbackImpl();

  /**
   * Creates a DeleteManager.
   * @param clusterMap The {@link ClusterMap} of the cluster.
   * @param responseHandler The {@link ResponseHandler} used to notify failures for failure detection.
   * @param notificationSystem The {@link NotificationSystem} used for notifying blob deletions.
   * @param routerConfig The {@link RouterConfig} containing the configs for the DeleteManager.
   * @param routerMetrics The {@link NonBlockingRouterMetrics} to be used for reporting metrics.
   * @param operationCompleteCallback The {@link OperationCompleteCallback} to use to complete operations.
   * @param time The {@link Time} instance to use.
   */
  DeleteManager(ClusterMap clusterMap, ResponseHandler responseHandler, NotificationSystem notificationSystem,
      RouterConfig routerConfig, NonBlockingRouterMetrics routerMetrics,
      OperationCompleteCallback operationCompleteCallback, Time time) {
    this.clusterMap = clusterMap;
    this.responseHandler = responseHandler;
    this.notificationSystem = notificationSystem;
    this.routerConfig = routerConfig;
    this.routerMetrics = routerMetrics;
    this.operationCompleteCallback = operationCompleteCallback;
    this.time = time;
    deleteOperations = Collections.newSetFromMap(new ConcurrentHashMap<DeleteOperation, Boolean>());
    correlationIdToDeleteOperation = new HashMap<Integer, DeleteOperation>();
  }

  /**
   * Submits a {@link DeleteOperation} to this {@code DeleteManager}.
   * @param blobIdString The blobId string to be deleted.
   * @param futureResult The {@link FutureResult} that will contain the result eventually and exception if any.
   * @param callback The {@link Callback} that will be called on completion of the request.
   */
  void submitDeleteBlobOperation(String blobIdString, FutureResult<Void> futureResult, Callback<Void> callback) {
    try {
      BlobId blobId = RouterUtils.getBlobIdFromString(blobIdString, clusterMap);
      DeleteOperation deleteOperation =
          new DeleteOperation(routerConfig, routerMetrics, responseHandler, blobId, futureResult, callback, time);
      deleteOperations.add(deleteOperation);
    } catch (RouterException e) {
      routerMetrics.operationDequeuingRate.mark();
      routerMetrics.deleteBlobErrorCount.inc();
      routerMetrics.countError(e);
      operationCompleteCallback.completeOperation(futureResult, callback, null, e);
    }
  }

  /**
   * Polls all delete operations and populates a list of {@link RequestInfo} to be sent to data nodes in order to
   * complete delete operations.
   * @param requestListToFill list to be filled with the requests created.
   */
  public void poll(List<RequestInfo> requestListToFill) {
    long startTime = time.milliseconds();
    requestRegistrationCallback.requestListToFill = requestListToFill;
    for (DeleteOperation op : deleteOperations) {
      boolean exceptionEncountered = false;
      try {
        op.poll(requestRegistrationCallback);
      } catch (Exception e) {
        exceptionEncountered = true;
        op.setOperationException(new RouterException("Delete poll encountered unexpected error", e,
            RouterErrorCode.UnexpectedInternalError));
      }
      if (exceptionEncountered || op.isOperationComplete()) {
        if (deleteOperations.remove(op)) {
          // In order to ensure that an operation is completed only once, call onComplete() only at the place where the
          // operation actually gets removed from the set of operations. See comment within close().
          onComplete(op);
        }
      }
    }
    routerMetrics.deleteManagerPollTimeMs.update(time.milliseconds() - startTime);
  }

  /**
   * Handles responses received for each of the {@link DeleteOperation} within this delete manager.
   * @param responseInfo the {@link ResponseInfo} containing the response.
   */
  void handleResponse(ResponseInfo responseInfo) {
    long startTime = time.milliseconds();
    int correlationId = ((DeleteRequest) responseInfo.getRequest()).getCorrelationId();
    DeleteOperation deleteOperation = correlationIdToDeleteOperation.remove(correlationId);
    // If it is still an active operation, hand over the response. Otherwise, ignore.
    if (deleteOperations.contains(deleteOperation)) {
      boolean exceptionEncountered = false;
      try {
        deleteOperation.handleResponse(responseInfo);
      } catch (Exception e) {
        exceptionEncountered = true;
        deleteOperation.setOperationException(
            new RouterException("Delete handleResponse encountered unexpected error", e,
                RouterErrorCode.UnexpectedInternalError));
      }
      if (exceptionEncountered || deleteOperation.isOperationComplete()) {
        if (deleteOperations.remove(deleteOperation)) {
          onComplete(deleteOperation);
        }
      }
      routerMetrics.deleteManagerHandleResponseTimeMs.update(time.milliseconds() - startTime);
    } else {
      routerMetrics.ignoredResponseCount.inc();
    }
  }

  /**
   * Called when the delete operation is completed. The {@code DeleteManager} also finishes the delete operation
   * by performing the callback and notification.
   * @param op The {@link DeleteOperation} that has completed.
   */
  void onComplete(DeleteOperation op) {
    Exception e = op.getOperationException();
    if (e == null) {
      notificationSystem.onBlobDeleted(op.getBlobId().getID());
    } else {
      routerMetrics.deleteBlobErrorCount.inc();
      routerMetrics.countError(e);
    }
    routerMetrics.operationDequeuingRate.mark();
    routerMetrics.deleteBlobOperationLatencyMs.update(time.milliseconds() - op.getSubmissionTimeMs());
    operationCompleteCallback
        .completeOperation(op.getFutureResult(), op.getCallback(), op.getOperationResult(), op.getOperationException());
  }

  /**
   * Closes the {@code DeleteManager}. A {@code DeleteManager} can be closed for only once. Any further close action
   * will have no effect.
   */
  void close() {
    for (DeleteOperation op : deleteOperations) {
      // There is a rare scenario where the operation gets removed from this set and gets completed concurrently by
      // the RequestResponseHandler thread when it is in poll() or handleResponse(). In order to avoid the completion
      // from happening twice, complete it here only if the remove was successful.
      if (deleteOperations.remove(op)) {
        Exception e = new RouterException("Aborted operation because Router is closed.", RouterErrorCode.RouterClosed);
        routerMetrics.operationDequeuingRate.mark();
        routerMetrics.operationAbortCount.inc();
        routerMetrics.deleteBlobErrorCount.inc();
        routerMetrics.countError(e);
        operationCompleteCallback.completeOperation(op.getFutureResult(), op.getCallback(), null, e);
      }
    }
  }
}