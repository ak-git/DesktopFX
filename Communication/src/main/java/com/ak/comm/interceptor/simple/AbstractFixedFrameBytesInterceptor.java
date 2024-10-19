package com.ak.comm.interceptor.simple;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.interceptor.AbstractBytesInterceptor;

import javax.annotation.Nonnegative;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.LinkedList;

public abstract class AbstractFixedFrameBytesInterceptor extends AbstractBytesInterceptor<BufferFrame, BufferFrame> {
  private final byte[] buffer;
  private int position = -1;

  protected AbstractFixedFrameBytesInterceptor(String name, BaudRate baudRate, @Nonnegative int frameLength) {
    super(name, baudRate, IGNORE_LIMIT);
    if (frameLength < 1) {
      throw new IllegalArgumentException("frameLength must be > 0, but found %d".formatted(frameLength));
    }
    buffer = new byte[frameLength];
  }

  @Override
  protected final Collection<BufferFrame> innerProcessIn(ByteBuffer src) {
    Collection<BufferFrame> responses = new LinkedList<>();
    while (src.hasRemaining()) {
      byte in = src.get();
      position++;
      if (position < buffer.length) {
        buffer[position] = in;
      }
      else if (check(buffer, in)) {
        logSkippedBytes(true);
        responses.add(new BufferFrame(buffer.clone(), ByteOrder.LITTLE_ENDIAN));
        position = 0;
        buffer[position] = in;
      }
      else {
        ignoreBuffer().put(buffer[0]);
        logSkippedBytes(false);
        System.arraycopy(buffer, 1, buffer, 0, buffer.length - 1);
        buffer[buffer.length - 1] = in;
        position = buffer.length - 1;
      }
    }
    return responses;
  }

  protected abstract boolean check(byte[] buffer, byte nextFrameStartByte);
}
