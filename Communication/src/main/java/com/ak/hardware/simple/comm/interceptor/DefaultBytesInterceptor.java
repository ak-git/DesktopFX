package com.ak.hardware.simple.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.interceptor.AbstractBytesInterceptor;

public final class DefaultBytesInterceptor extends AbstractBytesInterceptor<Integer, Byte> {
  public DefaultBytesInterceptor() {
    super("None", 1, (byte) 0);
  }

  @Nonnegative
  @Override
  public int write(@Nonnull ByteBuffer src) {
    super.write(src);
    src.rewind();
    int countBytes = 0;
    while (src.hasRemaining()) {
      bufferPublish().onNext(Byte.toUnsignedInt(src.get()));
      countBytes++;
    }
    return countBytes;
  }

  @Override
  protected void innerPut(@Nonnull ByteBuffer outBuffer, @Nonnull Byte aByte) {
    outBuffer.put(aByte);
  }
}
