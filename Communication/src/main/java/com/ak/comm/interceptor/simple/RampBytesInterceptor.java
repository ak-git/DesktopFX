package com.ak.comm.interceptor.simple;

import com.ak.comm.interceptor.BytesInterceptor;

import javax.annotation.Nonnegative;

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
public class RampBytesInterceptor extends AbstractFixedFrameBytesInterceptor {
  public RampBytesInterceptor(String name, BytesInterceptor.BaudRate baudRate, @Nonnegative int frameLength) {
    super(name, baudRate, frameLength);
  }

  @Override
  protected boolean check(byte[] buffer, byte nextFrameStartByte) {
    return (byte) (buffer[0] + 1) == nextFrameStartByte;
  }
}
