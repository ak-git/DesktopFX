package com.ak.comm.interceptor.simple;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.interceptor.AbstractBytesInterceptor;
import com.ak.comm.interceptor.BytesInterceptor;

/**
 * Ramp (from 0 to 255) at start byte protocol implementation and <b>fixed frame length</b>
 * <p>
 * Examples (frame length = 9 bytes):
 * <pre>
 *   BufferFrame[ <b>0x01</b>, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 ]
 *   BufferFrame[ <b>0x02</b>, 0x0a, 0x14, 0x1e, 0x28, 0x32, 0x3c, 0x46, 0x50 ]
 * </pre>
 * </p>
 */
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
        responses.add(new BufferFrame(bytes, ByteOrder.LITTLE_ENDIAN));

        byte saveStart = buffer[bufferIndex];
        Arrays.fill(buffer, (byte) 0);
        bufferIndex = 0;
        buffer[0] = saveStart;
      }
    }
    return responses;
  }
}
