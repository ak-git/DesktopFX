package com.ak.appliance.rcm.comm.interceptor;

import com.ak.comm.interceptor.simple.AbstractFixedFrameBytesInterceptor;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.EnumSet;
import java.util.Set;

public abstract class AbstractRcmBytesInterceptor extends AbstractFixedFrameBytesInterceptor {
  protected AbstractRcmBytesInterceptor(String name) {
    super(name, BaudRate.BR_38400, 20);
  }

  @Override
  public final Set<SerialParams> getSerialParams() {
    return EnumSet.of(SerialParams.CLEAR_DTR, SerialParams.DATA_BITS_7);
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  protected boolean check(byte[] buffer, byte nextFrameStartByte) {
    for (int i = 1; i < buffer.length; i++) {
      if (buffer[i] == (i & 0x01)) {
        return false;
      }
    }
    return (buffer[0] & 0x01) == 0 && (nextFrameStartByte & 0x01) == 0;
  }
}
