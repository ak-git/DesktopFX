package com.ak.appliance.rcms.comm.interceptor;

import com.ak.appliance.rcm.comm.interceptor.AbstractRcmBytesInterceptor;
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
    return (buffer[0] & 0x01) == 0 &&
        buffer[buffer.length - 1] == 0 &&
        (nextFrameStartByte & 0x01) == 0;
  }
}
