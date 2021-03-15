package com.ak.comm.interceptor.kleiber;

import javax.annotation.Nonnull;

import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.AbstractFixedFrameBytesInterceptor;

/**
 * Constant byte at start byte protocol implementation and <b>fixed frame length</b>
 * <p>
 * Examples (frame length = 34 bytes):
 * <pre>
 *   BufferFrame[ <b>0xAA</b>, Float-1 (4 bytes) ... Float-8 (4 bytes), 0xBB ]
 * </pre>
 * </p>
 */
public final class KleiberBytesInterceptor extends AbstractFixedFrameBytesInterceptor {
  private static final byte START = (byte) 0xaa;
  private static final byte STOP = (byte) 0xbb;

  public KleiberBytesInterceptor() {
    super("kleiber-myo", BytesInterceptor.BaudRate.BR_921600, 1 + 8 * Float.BYTES + 1);
  }

  @Override
  protected boolean check(@Nonnull byte[] buffer, byte nextFrameStartByte) {
    return buffer[0] == START && buffer[buffer.length - 1] == STOP && nextFrameStartByte == START;
  }
}
