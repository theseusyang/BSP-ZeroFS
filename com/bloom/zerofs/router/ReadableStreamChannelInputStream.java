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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.bloom.zerofs.api.router.Callback;
import com.bloom.zerofs.api.router.ReadableStreamChannel;
import com.bloom.zerofs.commons.ByteBufferAsyncWritableChannel;


/**
 *  Class that converts a (possibly non-blocking) {@link ReadableStreamChannel} into a blocking {@link InputStream}.
 *  <p/>
 *  This class is not thread-safe and will result in undefined behaviour if accesses to the stream are not synchronized.
 */
class ReadableStreamChannelInputStream extends InputStream {
  private final ByteBufferAsyncWritableChannel asyncWritableChannel = new ByteBufferAsyncWritableChannel();
  private final CloseWriteChannelCallback callback = new CloseWriteChannelCallback(asyncWritableChannel);
  private final ReadableStreamChannel readableStreamChannel;

  private ByteBuffer currentChunk = null;
  private volatile long bytesAvailable;

  /**
   * Create a ReadableStreamChannelInputStream with the given {@link ReadableStreamChannel}.
   * @param readableStreamChannel the {@link ReadableStreamChannel} that needs to be converted into an
   * {@link InputStream}.
   */
  public ReadableStreamChannelInputStream(ReadableStreamChannel readableStreamChannel) {
    this.readableStreamChannel = readableStreamChannel;
    bytesAvailable = readableStreamChannel.getSize();
    readableStreamChannel.readInto(asyncWritableChannel, callback);
  }

  @Override
  public int available() {
    return bytesAvailable < Integer.MAX_VALUE ? (int) bytesAvailable : Integer.MAX_VALUE;
  }

  @Override
  public int read()
      throws IOException {
    int data = -1;
    if (loadData()) {
      data = currentChunk.get() & 0xFF;
      bytesAvailable--;
    }
    return data;
  }

  @Override
  public int read(byte b[], int off, int len)
      throws IOException {
    if (b == null) {
      throw new NullPointerException();
    } else if (off < 0 || len < 0 || len > b.length - off) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return 0;
    }

    int startOff = off;
    while (len > 0 && loadData()) {
      int toRead = Math.min(len, currentChunk.remaining());
      currentChunk.get(b, off, toRead);
      len -= toRead;
      off += toRead;
      bytesAvailable -= toRead;
    }

    int bytesRead = off - startOff;
    if (bytesRead <= 0) {
      bytesRead = -1;
    }
    return bytesRead;
  }

  @Override
  public void close()
      throws IOException {
    readableStreamChannel.close();
    asyncWritableChannel.close();
  }

  /**
   * Loads more data for reading. Blocks until data is either available or no more data is expected.
   * @return {@code true} if data is available for reading. {@link false} otherwise.
   * @throws IllegalStateException if the wait for the next chunk is interrupted.
   * @throws IOException if there is any problem with I/O.
   */
  private boolean loadData()
      throws IOException {
    if (currentChunk == null || !currentChunk.hasRemaining()) {
      if (currentChunk != null) {
        asyncWritableChannel.resolveOldestChunk(null);
      }
      try {
        currentChunk = asyncWritableChannel.getNextChunk();
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
    if (currentChunk == null) {
      if (callback.exception != null) {
        if (callback.exception instanceof IOException) {
          throw (IOException) callback.exception;
        } else {
          throw new IllegalStateException(callback.exception);
        }
      } else if (bytesAvailable != 0) {
        throw new IllegalStateException("All the bytes available could not be read");
      }
    }
    return currentChunk != null;
  }

  /**
   * Callback for {@link ByteBufferAsyncWritableChannel} that closes the channel on
   * {@link #onCompletion(Long, Exception)}.
   */
  private static class CloseWriteChannelCallback implements Callback<Long> {
    /**
     * Stores any exception that occurred.
     */
    public Exception exception = null;
    private final ByteBufferAsyncWritableChannel channel;

    /**
     * Creates a callback to close {@code channel} on {@link #onCompletion(Long, Exception)}.
     * @param channel the {@link ByteBufferAsyncWritableChannel} that needs to be closed.
     */
    public CloseWriteChannelCallback(ByteBufferAsyncWritableChannel channel) {
      this.channel = channel;
    }

    @Override
    public void onCompletion(Long result, Exception exception) {
      this.exception = exception;
      channel.close();
    }
  }
}

