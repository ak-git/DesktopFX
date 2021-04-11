package com.ak.comm.interceptor.simple;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.interceptor.AbstractBytesInterceptor;

public final class StringBytesInterceptor extends AbstractBytesInterceptor<BufferFrame, String> {
  private static final int MAX_LEN = 6;
  private static final byte STOP = '\n';
  private final StringBuilder frame = new StringBuilder(MAX_LEN);

  public StringBytesInterceptor(@Nonnull String name) {
    super(name, BaudRate.BR_115200, null, MAX_LEN);
  }

  @Nonnull
  @Override
  protected Collection<String> innerProcessIn(@Nonnull ByteBuffer src) {
    Collection<String> responses = new LinkedList<>();
    while (src.hasRemaining()) {
      byte in = src.get();
      frame.append((char) in);
      if (in == STOP) {
        logSkippedBytes(true);
        responses.add(frame.toString().trim());
        frame.delete(0, frame.length());
      }
      else if (frame.length() == MAX_LEN) {
        ignoreBuffer().put((byte) frame.charAt(0));
        logSkippedBytes(false);
        frame.deleteCharAt(0);
      }
    }
    return responses;
  }
}
