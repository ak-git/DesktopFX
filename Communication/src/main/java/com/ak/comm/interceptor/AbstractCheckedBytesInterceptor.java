package com.ak.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractCheckedBytesInterceptor<B extends AbstractCheckedBuilder<RESPONSE>, RESPONSE, REQUEST extends AbstractBufferFrame>
    extends AbstractBytesInterceptor<RESPONSE, REQUEST> {
  @Nonnull
  private final B responseBuilder;

  protected AbstractCheckedBytesInterceptor(@Nonnull String name, @Nullable REQUEST pingRequest,
                                            @Nonnull B responseBuilder) {
    super(name, responseBuilder.buffer().capacity(), pingRequest);
    this.responseBuilder = responseBuilder;
  }

  @Override
  public final int write(@Nonnull ByteBuffer src) {
    src.rewind();
    int countResponse = 0;
    ByteBuffer buffer = responseBuilder.buffer();
    while (src.hasRemaining()) {
      byte b = src.get();

      for (int i = 0; i < 2; i++) {
        buffer.put(b);
        if (responseBuilder.is(b)) {
          break;
        }
        else {
          buffer.clear();
        }
      }

      if (!buffer.hasRemaining()) {
        buffer.rewind();
        RESPONSE response = responseBuilder.build();
        if (response != null) {
          bufferPublish().onNext(response);
          countResponse++;
        }
        buffer.clear();
      }
    }
    return countResponse;
  }

  @Override
  final void innerPut(@Nonnull ByteBuffer outBuffer, @Nonnull REQUEST request) {
    request.writeTo(outBuffer);
  }
}
