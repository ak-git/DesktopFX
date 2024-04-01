package com.ak.appliance.sktbpr.comm.interceptor;

import com.ak.appliance.sktbpr.comm.bytes.SKTBRequest;
import com.ak.appliance.sktbpr.comm.bytes.SKTBResponse;
import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;

import java.util.EnumSet;
import java.util.Set;

public final class SKTBBytesInterceptor extends AbstractCheckedBytesInterceptor<SKTBRequest, SKTBResponse, SKTBResponse.Builder> {
  public SKTBBytesInterceptor() {
    super("SKTB-PR", BaudRate.BR_57600, new SKTBResponse.Builder(), SKTBRequest.NONE);
  }

  @Override
  public Set<SerialParams> getSerialParams() {
    return EnumSet.of(SerialParams.ODD_PARITY);
  }
}
