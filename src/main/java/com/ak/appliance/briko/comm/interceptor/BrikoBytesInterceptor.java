package com.ak.appliance.briko.comm.interceptor;

import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;

public final class BrikoBytesInterceptor extends RampBytesInterceptor {
  public BrikoBytesInterceptor() {
    super("Briko-MAX-12", BytesInterceptor.BaudRate.BR_921600, 1 + 4 * 12 + 4 * 12 + 3 * 12 + 3 * 6 + 1);
  }

  @Override
  protected boolean check(byte[] buffer, byte nextFrameStartByte) {
    return super.check(buffer, nextFrameStartByte) && buffer[1] == 0b001_00001;
  }
}
