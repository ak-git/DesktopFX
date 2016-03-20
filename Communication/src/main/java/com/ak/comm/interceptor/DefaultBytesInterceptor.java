package com.ak.comm.interceptor;

import java.nio.ByteBuffer;

public final class DefaultBytesInterceptor extends AbstractBytesInterceptor<Integer, Byte> {
  public DefaultBytesInterceptor() {
    super(1, (byte) 0);
  }

  @Override
  public int write(ByteBuffer src) {
    src.rewind();
    int countBytes = 0;
    while (src.hasRemaining()) {
      bufferPublish().onNext(Byte.toUnsignedInt(src.get()));
      countBytes++;
    }
    return countBytes;
  }

  @Override
  public ByteBuffer put(Byte aByte) {
    outBuffer().clear();
    outBuffer().put(aByte).flip();
    return outBuffer();
  }
}
