package com.ak.comm.interceptor.simple;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.interceptor.AbstractBytesInterceptor;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;

public final class StringBytesInterceptor extends AbstractBytesInterceptor<BufferFrame, String> {
  private static final byte STOP = '\n';
  private final int maxLen;
  private final StringBuilder frame;

  public StringBytesInterceptor(String name, BaudRate baudRate, int maxLen) {
    super(name, baudRate, maxLen);
    this.maxLen = maxLen;
    frame = new StringBuilder(maxLen);
  }

  @Override
  protected Collection<String> innerProcessIn(ByteBuffer src) {
    Collection<String> responses = new LinkedList<>();
    while (src.hasRemaining()) {
      byte in = src.get();
      frame.append((char) in);
      if (in == STOP) {
        logSkippedBytes(true);
        responses.add(frame.toString().strip());
        frame.delete(0, frame.length());
      }
      else if (frame.length() == maxLen) {
        ignoreBuffer().put((byte) frame.charAt(0));
        logSkippedBytes(false);
        frame.deleteCharAt(0);
      }
    }
    return responses;
  }
}
