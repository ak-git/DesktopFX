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
  private int position = -1;

  public RampBytesInterceptor(@Nonnull BytesInterceptor.BaudRate baudRate, int frameLength) {
    super(baudRate, null);
    if (frameLength < 1) {
      throw new IllegalArgumentException(String.format("frameLength must be > 0, but found %d", frameLength));
    }
    buffer = new byte[frameLength];
  }

  @Override
  protected Collection<BufferFrame> innerProcessIn(@Nonnull ByteBuffer src) {
    Collection<BufferFrame> responses = new LinkedList<>();
    while (src.hasRemaining()) {
      byte in = src.get();
      position++;
      if (position < buffer.length) {
        buffer[position] = in;
      }
      else {
        if (((byte) (buffer[0] + 1)) == in) {
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
