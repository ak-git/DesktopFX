package com.ak.hardware.nmis.comm.interceptor;

import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;

public final class NmisBytesInterceptor extends AbstractCheckedBytesInterceptor<NmisResponseFrame.Builder, NmisResponseFrame, NmisRequest> {
  public NmisBytesInterceptor() {
    super("NMIS", NmisRequest.Sequence.CATCH_100.build(), new NmisResponseFrame.Builder());
  }
}
