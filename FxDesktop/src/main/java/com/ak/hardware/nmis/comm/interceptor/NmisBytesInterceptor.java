package com.ak.hardware.nmis.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;

public final class NmisBytesInterceptor extends AbstractCheckedBytesInterceptor<NmisProtocolByte, NmisResponseFrame, NmisRequest> {
  public NmisBytesInterceptor() {
    super("NMIS", NmisProtocolByte.MAX_CAPACITY, NmisRequest.Sequence.CATCH_100.build(), NmisProtocolByte.CHECKED_BYTES);
  }

  @Nullable
  @Override
  protected NmisResponseFrame newResponse(@Nonnull ByteBuffer byteBuffer) {
    byte[] array = new byte[byteBuffer.limit()];
    byteBuffer.get(array);
    return NmisResponseFrame.newInstance(array);
  }
}
