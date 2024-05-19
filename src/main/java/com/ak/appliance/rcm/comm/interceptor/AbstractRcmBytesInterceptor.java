package com.ak.appliance.rcm.comm.interceptor;

import com.ak.comm.interceptor.simple.AbstractFixedFrameBytesInterceptor;

import java.util.EnumSet;
import java.util.Set;

public abstract class AbstractRcmBytesInterceptor extends AbstractFixedFrameBytesInterceptor {
  protected AbstractRcmBytesInterceptor(String name) {
    super(name, BaudRate.BR_38400, 20);
  }

  @Override
  public final Set<SerialParams> getSerialParams() {
    return EnumSet.of(SerialParams.CLEAR_DTR);
  }
}
