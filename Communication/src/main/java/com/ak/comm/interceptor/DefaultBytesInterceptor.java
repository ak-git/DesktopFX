package com.ak.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

public final class DefaultBytesInterceptor extends AbstractBytesInterceptor<Integer, Byte> {
  public DefaultBytesInterceptor() {
    super("None", 1, (byte) 0);
  }

  @Override
  public int write(@Nonnull ByteBuffer src) {
    src.rewind();
    int countBytes = 0;
    while (src.hasRemaining()) {
      bufferPublish().onNext(Byte.toUnsignedInt(src.get()));
      countBytes++;
    }
    return countBytes;
  }

  @Override
  void innerPut(@Nonnull ByteBuffer outBuffer, @Nonnull Byte aByte) {
    outBuffer.put(aByte);
  }
}
