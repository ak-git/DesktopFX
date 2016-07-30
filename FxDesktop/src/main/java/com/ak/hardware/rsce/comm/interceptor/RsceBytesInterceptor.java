package com.ak.hardware.rsce.comm.interceptor;

import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;

public final class RsceBytesInterceptor extends AbstractCheckedBytesInterceptor<RsceCommandFrame.ResponseBuilder, RsceCommandFrame, RsceCommandFrame> {
  public RsceBytesInterceptor() {
    super("RSCE", RsceCommandFrame.off(RsceCommandFrame.Control.ALL), new RsceCommandFrame.ResponseBuilder());
  }
}
