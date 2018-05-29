package com.ak.comm.interceptor.simple;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.interceptor.AbstractBytesInterceptor;

abstract class AbstractFixedFrameBytesInterceptor extends AbstractBytesInterceptor<BufferFrame, BufferFrame> {
  @Nonnull
  private final byte[] buffer;
  @Nonnull
  private final IntUnaryOperator nextStart;
  private int position = -1;

  AbstractFixedFrameBytesInterceptor(@Nonnull BaudRate baudRate, int frameLength, @Nonnull IntUnaryOperator nextStart) {
    super(baudRate, null, IGNORE_LIMIT);
    this.nextStart = nextStart;
    if (frameLength < 1) {
      throw new IllegalArgumentException(String.format("frameLength must be > 0, but found %d", frameLength));
    }
    buffer = new byte[frameLength];
  }

  @Override
  protected final Collection<BufferFrame> innerProcessIn(@Nonnull ByteBuffer src) {
    Collection<BufferFrame> responses = new LinkedList<>();
    while (src.hasRemaining()) {
      byte in = src.get();
      position++;
      if (position < buffer.length) {
        buffer[position] = in;
      }
      else {
        if (((byte) nextStart.applyAsInt(buffer[0])) == in) {
          logSkippedBytes(true);
          responses.add(new BufferFrame(Arrays.copyOf(buffer, buffer.length), ByteOrder.LITTLE_ENDIAN));
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
    }
    return responses;
  }
}
