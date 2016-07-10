package com.ak.hardware.rsce.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.EnumSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;

final class RsceBytesInterceptor extends AbstractCheckedBytesInterceptor<RsceCommandFrame.ProtocolByte, RsceCommandFrame, RsceCommandFrame> {
  RsceBytesInterceptor() {
    super("RSCE", RsceCommandFrame.MAX_CAPACITY, RsceCommandFrame.off(RsceCommandFrame.Control.ALL), EnumSet.allOf(RsceCommandFrame.ProtocolByte.class));
  }

  @Nullable
  @Override
  protected RsceCommandFrame newResponse(@Nonnull ByteBuffer byteBuffer) {
    return RsceCommandFrame.newInstance(byteBuffer);
  }
}
