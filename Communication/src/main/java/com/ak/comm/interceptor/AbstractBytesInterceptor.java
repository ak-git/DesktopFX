package com.ak.comm.interceptor;

import java.nio.ByteBuffer;

import com.ak.comm.core.AbstractService;

abstract class AbstractBytesInterceptor<FROM, TO> extends AbstractService<FROM> implements BytesInterceptor<FROM, TO> {
  private final ByteBuffer outBuffer;

  AbstractBytesInterceptor(int outBufferSize) {
    outBuffer = ByteBuffer.allocate(outBufferSize);
  }

  @Override
  public final boolean isOpen() {
    return bufferPublish().hasObservers();
  }

  @Override
  public final void close() {
    bufferPublish().onCompleted();
  }

  final ByteBuffer outBuffer() {
    return outBuffer;
  }
}
