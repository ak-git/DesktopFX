package com.ak.comm.interceptor.briko;

import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static java.lang.Integer.BYTES;

@Component("briko-interceptor")
@Profile("briko")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class BrikoBytesInterceptor extends RampBytesInterceptor {
  public BrikoBytesInterceptor() {
    super("Briko-Stand", BytesInterceptor.BaudRate.BR_921600, 32);
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
