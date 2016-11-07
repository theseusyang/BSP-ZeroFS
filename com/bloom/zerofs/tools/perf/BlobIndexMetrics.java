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
package com.bloom.zerofs.tools.perf;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import com.bloom.zerofs.api.config.StoreConfig;
import com.bloom.zerofs.api.store.StoreException;
import com.bloom.zerofs.api.store.StoreKey;
import com.bloom.zerofs.api.store.StoreKeyFactory;
import com.bloom.zerofs.commons.BlobId;
import com.bloom.zerofs.messageformat.BlobStoreHardDelete;
import com.bloom.zerofs.messageformat.BlobStoreRecovery;
import com.bloom.zerofs.store.FileSpan;
import com.bloom.zerofs.store.IndexEntry;
import com.bloom.zerofs.store.IndexValue;
import com.bloom.zerofs.store.Log;
import com.bloom.zerofs.store.PersistentIndex;
import com.bloom.zerofs.store.StoreMetrics;
import com.bloom.zerofs.tools.Scheduler;
import com.bloom.zerofs.tools.SystemTime;
import com.codahale.metrics.MetricRegistry;


class BlobIndexMetrics extends PersistentIndex {
  private Object lock = new Object();
  private boolean enableVerboseLogging;
  private AtomicLong totalWrites;
  private AtomicLong totalTimeTaken;
  private AtomicLong lastOffsetUsed;
  private AtomicLong totalReads;
  private FileWriter writer;
  private String datadir;

  public BlobIndexMetrics(String datadir, Scheduler scheduler, Log log, boolean enableVerboseLogging,
      AtomicLong totalWrites, AtomicLong totalTimeTaken, AtomicLong totalReads, StoreConfig config, FileWriter writer,
      StoreKeyFactory factory)
      throws StoreException {
    super(datadir, scheduler, log, config, factory, new BlobStoreRecovery(), new BlobStoreHardDelete(),
        new StoreMetrics(datadir, new MetricRegistry()), SystemTime.getInstance());
    this.enableVerboseLogging = enableVerboseLogging;
    this.lastOffsetUsed = new AtomicLong(0);
    this.totalWrites = totalWrites;
    this.totalTimeTaken = totalTimeTaken;
    this.totalReads = totalReads;
    this.writer = writer;
    this.datadir = datadir;
  }

  public void addToIndexRandomData(BlobId id)
      throws StoreException {

    synchronized (lock) {
      long startTimeInMs = System.currentTimeMillis();
      long size = new Random().nextInt(10000);
      IndexEntry entry = new IndexEntry(id, new IndexValue(size, lastOffsetUsed.get(), (byte) 1, 1000));
      lastOffsetUsed.addAndGet(size);
      long offset = getCurrentEndOffset();
      addToIndex(entry, new FileSpan(offset, offset + entry.getValue().getSize()));
      long endTimeInMs = System.currentTimeMillis();
      IndexValue value = findKey(id);
      if (value == null) {
        System.out.println("error: value is null");
      }
      if (enableVerboseLogging) {
        System.out.println("Time taken to add to the index in Ms - " + (endTimeInMs - startTimeInMs));
      }
      totalTimeTaken.addAndGet(endTimeInMs - startTimeInMs);
      try {
        if (writer != null) {
          writer.write("blobid-" + datadir + "-" + id + "\n");
        }
      } catch (Exception e) {
        System.out.println("error while logging");
      }
    }
    totalWrites.incrementAndGet();
    if (totalWrites.get() % 1000 == 0) {
      System.out.println("number of indexes created " + indexes.size());
    }
  }

  public void addToIndexRandomData(List<BlobId> ids)
      throws StoreException {
    ArrayList<IndexEntry> list = new ArrayList<IndexEntry>(ids.size());
    for (int i = 0; i < list.size(); i++) {
      IndexEntry entry = new IndexEntry(ids.get(i), new IndexValue(1000, 1000, (byte) 1, 1000));
      list.add(entry);
      try {
        if (writer != null) {
          writer.write("blobid-" + datadir + "-" + ids.get(i) + "\n");
        }
      } catch (Exception e) {
        System.out.println("error while logging");
      }
    }
    long startTimeInMs = System.currentTimeMillis();
    synchronized (lock) {
      addToIndex(list, new FileSpan(getCurrentEndOffset(), getCurrentEndOffset() + 1000));
    }
    long endTimeInMs = System.currentTimeMillis();
    System.out.println("Time taken to add to the index all the entries - " + (endTimeInMs - startTimeInMs));
  }

  public boolean exists(StoreKey key)
      throws StoreException {
    System.out.println("data dir " + datadir + " searching id " + key);
    long startTimeMs = System.currentTimeMillis();
    boolean exist = super.findKey(key) != null;
    long endTimeMs = System.currentTimeMillis();
    System.out.println("Time to find an entry exist - " + (endTimeMs - startTimeMs));
    totalTimeTaken.addAndGet(endTimeMs - startTimeMs);
    totalReads.incrementAndGet();
    return exist;
  }

  public void markAsDeleted(StoreKey id, long logEndOffset)
      throws StoreException {
    long startTimeMs = System.currentTimeMillis();
    super.markAsDeleted(id, new FileSpan(0, logEndOffset));
    long endTimeMs = System.currentTimeMillis();
    System.out.println("Time to delete an entry - " + (endTimeMs - startTimeMs));
  }
}
