package com.ak.appliance.rcm.comm.interceptor;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Profile({"rcms", "rcms-calibration"})
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class RcmsBytesInterceptor extends AbstractRcmBytesInterceptor {
  public RcmsBytesInterceptor() {
    super("rcms");
  }

  @Override
  protected boolean check(byte[] buffer, byte nextFrameStartByte) {
    return buffer[buffer.length - 1] == 0 && super.check(buffer, nextFrameStartByte);
  }
}
