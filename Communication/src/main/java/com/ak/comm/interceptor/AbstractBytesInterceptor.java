package com.ak.comm.interceptor;

import java.nio.ByteBuffer;

import com.ak.comm.core.AbstractService;

abstract class AbstractBytesInterceptor<FROM, TO> extends AbstractService<FROM> implements BytesInterceptor<FROM, TO> {
  private final ByteBuffer outBuffer;
  private final TO startCommand;

  AbstractBytesInterceptor(int outBufferSize, TO startCommand) {
    outBuffer = ByteBuffer.allocate(outBufferSize);
    this.startCommand = startCommand;
  }

  @Override
  public final boolean isOpen() {
    return bufferPublish().hasObservers();
  }

  @Override
  public final void close() {
    bufferPublish().onCompleted();
  }

  @Override
  public final TO getStartCommand() {
    return startCommand;
  }

  final ByteBuffer outBuffer() {
    return outBuffer;
  }
}
