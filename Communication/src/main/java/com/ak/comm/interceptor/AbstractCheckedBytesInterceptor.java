package com.ak.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractCheckedBytesInterceptor<RESPONSE, REQUEST extends AbstractBufferFrame>
    extends AbstractBytesInterceptor<RESPONSE, REQUEST> {
  private final ByteBuffer byteBuffer;

  protected AbstractCheckedBytesInterceptor(String name, int maxCapacity, REQUEST pingRequest) {
    super(name, maxCapacity, pingRequest);
    byteBuffer = ByteBuffer.allocate(maxCapacity);
  }

  protected final ByteBuffer byteBuffer() {
    return byteBuffer;
  }

  @Override
  public final int write(@Nonnull ByteBuffer src) {
    src.rewind();
    int countResponse = 0;
    while (src.hasRemaining()) {
      byte b = src.get();
      if (!check(b)) {
        byteBuffer().put(b);
      }

      if (!byteBuffer().hasRemaining()) {
        byteBuffer().rewind();
        RESPONSE response = newResponse();
        if (response != null) {
          bufferPublish().onNext(response);
          countResponse++;
        }
        byteBuffer().clear();
      }
    }
    return countResponse;
  }

  protected abstract boolean check(byte b);

  @Nullable
  protected abstract RESPONSE newResponse();

  @Override
  protected final void innerPut(@Nonnull ByteBuffer outBuffer, @Nonnull REQUEST request) {
    request.writeTo(outBuffer);
  }
}
