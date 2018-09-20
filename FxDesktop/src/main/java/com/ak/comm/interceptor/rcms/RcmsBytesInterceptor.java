package com.ak.comm.interceptor.rcms;

import com.ak.comm.interceptor.simple.AbstractFixedFrameBytesInterceptor;

public final class RcmsBytesInterceptor extends AbstractFixedFrameBytesInterceptor {
  public RcmsBytesInterceptor() {
    super(BaudRate.BR_38400, 20);
  }
}
