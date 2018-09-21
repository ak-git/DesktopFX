package com.ak.comm.interceptor.rcm;

import javax.annotation.Nonnull;

import com.ak.comm.interceptor.simple.AbstractFixedFrameBytesInterceptor;

public final class RcmBytesInterceptor extends AbstractFixedFrameBytesInterceptor {
  public RcmBytesInterceptor() {
    super(BaudRate.BR_38400, 20);
  }

  @Override
  protected boolean check(@Nonnull byte[] buffer, byte nextFrameStartByte) {
    return (buffer[0] & 0x01) == 0 && (buffer[buffer.length - 1] & 0x01) == 0 && ((nextFrameStartByte & 0x01) == 0);
  }
}
