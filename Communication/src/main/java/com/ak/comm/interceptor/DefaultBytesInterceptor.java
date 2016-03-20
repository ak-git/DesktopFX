package com.ak.comm.interceptor;

import java.nio.ByteBuffer;

public final class DefaultBytesInterceptor extends AbstractBytesInterceptor<Integer> {
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
}
