package com.ak.hardware.tnmi.comm.interceptor;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;

@Immutable
public final class TnmiBytesInterceptor extends AbstractCheckedBytesInterceptor<TnmiResponseFrame, TnmiRequest> {
  public TnmiBytesInterceptor() {
    super("TNMI", TnmiProtocolByte.MAX_CAPACITY, TnmiRequest.Sequence.CATCH_100.build());
  }

  @Override
  protected boolean check(byte b) {
    boolean skip = false;
    for (TnmiProtocolByte checkedByte : TnmiProtocolByte.CHECKED_BYTES) {
      if (byteBuffer().position() == checkedByte.ordinal()) {
        if (checkedByte.is(b)) {
          if (checkedByte == TnmiProtocolByte.LEN) {
            byteBuffer().limit(b + 4);
          }
        }
        else {
          byteBuffer().clear();
          skip = !TnmiProtocolByte.START.is(b);
        }
        break;
      }
    }
    return skip;
  }

  @Nullable
  @Override
  protected TnmiResponseFrame newResponse() {
    byte[] array = new byte[byteBuffer().limit()];
    byteBuffer().get(array);
    return TnmiResponseFrame.newInstance(array);
  }
}
