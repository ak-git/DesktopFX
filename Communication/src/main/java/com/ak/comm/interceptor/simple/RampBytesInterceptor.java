package com.ak.comm.interceptor.simple;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.interceptor.AbstractBytesInterceptor;
import com.ak.comm.interceptor.BytesInterceptor;

public final class RampBytesInterceptor extends AbstractBytesInterceptor<BufferFrame, BufferFrame> {
  @Nonnull
  private final byte[] buffer;
  private int bufferIndex = -1;
  private int ignoreIndex = -1;

  public RampBytesInterceptor(@Nonnull BytesInterceptor.BaudRate baudRate, int frameLength) {
    super(baudRate, null);
    if (frameLength < 1) {
      throw new IllegalArgumentException(String.format("frameLength must be > 0, but found %d", frameLength));
    }
    buffer = new byte[frameLength + 1];
  }

  @Override
  protected Collection<BufferFrame> innerProcessIn(@Nonnull ByteBuffer src) {
    Collection<BufferFrame> responses = new LinkedList<>();
    while (src.hasRemaining()) {
      bufferIndex++;
      bufferIndex %= buffer.length;
      ignoreIndex++;
      if (++ignoreIndex >= buffer.length) {
        ignoreBuffer().put(buffer[bufferIndex]);
        logSkippedBytes(false);
      }
      buffer[bufferIndex] = src.get();

      if (((byte) (buffer[(bufferIndex + 1) % buffer.length] + 1)) == buffer[bufferIndex]) {
        logSkippedBytes(true);
        ignoreIndex = 0;

        byte[] bytes = new byte[buffer.length - 1];
        for (int i = 0; i < bytes.length; i++) {
          bytes[i] = buffer[(bufferIndex + 1 + i) % buffer.length];
        }
        responses.add(new BufferFrame(bytes));

        byte saveStart = buffer[bufferIndex];
        Arrays.fill(buffer, (byte) 0);
        bufferIndex = 0;
        buffer[0] = saveStart;
      }
    }
    return responses;
  }
}
