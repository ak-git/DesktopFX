package com.ak.comm.interceptor.rcm;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.ak.comm.interceptor.simple.AbstractFixedFrameBytesInterceptor;

public final class RcmBytesInterceptor extends AbstractFixedFrameBytesInterceptor {
  public RcmBytesInterceptor() {
    super(BaudRate.BR_38400, 20);
  }

  @Nonnull
  @Override
  public Set<SerialParams> getSerialParams() {
    return EnumSet.of(SerialParams.CLEAR_DTR);
  }

  @Override
  protected boolean check(@Nonnull byte[] buffer, byte nextFrameStartByte) {
    return (buffer[0] & 0x01) == 0 && (buffer[buffer.length - 1] & 0x01) == 0 && ((nextFrameStartByte & 0x01) == 0);
  }
}
