package com.ak.comm.interceptor.briko;

import javax.annotation.Nonnull;
import javax.inject.Named;

import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Named
@Profile("briko")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class BrikoBytesInterceptor extends RampBytesInterceptor {
  public BrikoBytesInterceptor() {
    super("Briko-Stand", BytesInterceptor.BaudRate.BR_921600, 32);
  }

  @Override
  protected boolean check(@Nonnull byte[] buffer, byte nextFrameStartByte) {
    return super.check(buffer, nextFrameStartByte) && (buffer[1] == 0x20) && (buffer[2] == (byte) 0xC1);
  }
}
