package com.ak.appliance.rcm.comm.interceptor;

public final class RcmsBytesInterceptor extends AbstractRcmBytesInterceptor {
  public RcmsBytesInterceptor() {
    super("rcms");
  }

  @Override
  protected boolean check(byte[] buffer, byte nextFrameStartByte) {
    return buffer[buffer.length - 1] == 0 && super.check(buffer, nextFrameStartByte);
  }
}
