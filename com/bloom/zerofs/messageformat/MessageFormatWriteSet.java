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
package com.bloom.zerofs.messageformat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import com.bloom.zerofs.api.store.MessageInfo;
import com.bloom.zerofs.api.store.MessageWriteSet;
import com.bloom.zerofs.api.store.Write;
import com.bloom.zerofs.tools.ByteBufferInputStream;


/**
 * A message write set that writes to the underlying write interface
 */
public class MessageFormatWriteSet implements MessageWriteSet {

  private final InputStream streamToWrite;
  private long sizeToWrite;
  private List<MessageInfo> streamInfo;

  public MessageFormatWriteSet(InputStream streamToWrite, List<MessageInfo> streamInfo, boolean materializeStream)
      throws IOException {
    sizeToWrite = 0;
    for (MessageInfo info : streamInfo) {
      sizeToWrite += info.getSize();
    }
    this.streamInfo = streamInfo;
    if(materializeStream){
      ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(streamToWrite, (int)sizeToWrite);
      this.streamToWrite = byteBufferInputStream;
    }
    else{
      this.streamToWrite = streamToWrite;
    }
  }

  @Override
  public long writeTo(Write writeChannel)
      throws IOException {
    ReadableByteChannel readableByteChannel = Channels.newChannel(streamToWrite);
    writeChannel.appendFrom(readableByteChannel, sizeToWrite);
    return sizeToWrite;
  }

  @Override
  public List<MessageInfo> getMessageSetInfo() {
    return streamInfo;
  }
}
