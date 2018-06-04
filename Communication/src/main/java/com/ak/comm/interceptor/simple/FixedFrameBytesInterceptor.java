package com.ak.comm.interceptor.simple;

import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnull;

/**
 * Constant byte at start byte protocol implementation and <b>fixed frame length</b>
 * <p>
 * Examples (frame length = 9 bytes):
 * <pre>
 *   BufferFrame[ <b>0xAA</b>, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 ]
 *   BufferFrame[ <b>0xAA</b>, 0x0a, 0x14, 0x1e, 0x28, 0x32, 0x3c, 0x46, 0x50 ]
 * </pre>
 * </p>
 */
public final class FixedFrameBytesInterceptor extends AbstractFixedFrameBytesInterceptor {
  public FixedFrameBytesInterceptor(@Nonnull BaudRate baudRate, int frameLength) {
    super(baudRate, frameLength, IntUnaryOperator.identity());
  }
}