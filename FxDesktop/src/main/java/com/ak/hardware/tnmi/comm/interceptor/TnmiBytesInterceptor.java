package com.ak.hardware.tnmi.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;

public final class TnmiBytesInterceptor extends AbstractCheckedBytesInterceptor<TnmiProtocolByte, TnmiResponseFrame, TnmiRequest> {
  public TnmiBytesInterceptor() {
    super("TNMI", TnmiProtocolByte.MAX_CAPACITY, TnmiRequest.Sequence.CATCH_100.build(), TnmiProtocolByte.CHECKED_BYTES);
  }

  @Nullable
  @Override
  protected TnmiResponseFrame newResponse(@Nonnull ByteBuffer byteBuffer) {
    byte[] array = new byte[byteBuffer.limit()];
    byteBuffer.get(array);
    return TnmiResponseFrame.newInstance(array);
  }
}
