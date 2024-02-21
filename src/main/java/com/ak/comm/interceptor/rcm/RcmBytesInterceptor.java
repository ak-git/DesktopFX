package com.ak.comm.interceptor.rcm;

import com.ak.comm.interceptor.simple.AbstractFixedFrameBytesInterceptor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
@Profile({"rcm", "rcm-calibration"})
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class RcmBytesInterceptor extends AbstractFixedFrameBytesInterceptor {
  public RcmBytesInterceptor() {
    super("RheoCardioMonitor", BaudRate.BR_38400, 20);
  }

  @Override
  public Set<SerialParams> getSerialParams() {
    return EnumSet.of(SerialParams.CLEAR_DTR);
  }

  @Override
  protected boolean check(byte[] buffer, byte nextFrameStartByte) {
    return (buffer[0] & 0x01) == 0 && (buffer[buffer.length - 1] & 0x01) == 0 && ((nextFrameStartByte & 0x01) == 0);
  }
}
