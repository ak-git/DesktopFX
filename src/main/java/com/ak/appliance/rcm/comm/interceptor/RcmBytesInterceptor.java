package com.ak.appliance.rcm.comm.interceptor;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Profile({"rcm", "rcm-calibration"})
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class RcmBytesInterceptor extends AbstractRcmBytesInterceptor {
  public RcmBytesInterceptor() {
    super("rcm");
  }

  @Override
  protected boolean check(byte[] buffer, byte nextFrameStartByte) {
    for (int i = 1; i < buffer.length; i++) {
      if (buffer[i] == (i & 0x01)) {
        return false;
      }
    }
    return (buffer[0] & 0x01) == 0 && (nextFrameStartByte & 0x01) == 0;
  }
}
