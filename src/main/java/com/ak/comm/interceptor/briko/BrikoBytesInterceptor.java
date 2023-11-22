package com.ak.comm.interceptor.briko;

import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;

import javax.annotation.Nonnull;

import static java.lang.Integer.BYTES;

public final class BrikoBytesInterceptor extends RampBytesInterceptor {
  public BrikoBytesInterceptor() {
    super("Briko-Black-Stand", BytesInterceptor.BaudRate.BR_921600, 32);
  }

  @Override
  protected boolean check(@Nonnull byte[] buffer, byte nextFrameStartByte) {
    if (super.check(buffer, nextFrameStartByte) && (buffer[1] == 0x20)) {
      for (int i = 0; i < 6; i++) {
        if (buffer[2 + i * (1 + BYTES)] != (byte) (0xC1 + i)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
}
