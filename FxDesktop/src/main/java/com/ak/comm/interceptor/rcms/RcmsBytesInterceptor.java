package com.ak.comm.interceptor.rcms;

import java.util.function.IntUnaryOperator;

import com.ak.comm.interceptor.simple.AbstractFixedFrameBytesInterceptor;

public final class RcmsBytesInterceptor extends AbstractFixedFrameBytesInterceptor {
  public RcmsBytesInterceptor() {
    super(BaudRate.BR_38400, 20, IntUnaryOperator.identity());
  }
}
