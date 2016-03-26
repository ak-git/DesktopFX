package com.ak.comm.interceptor;

import java.nio.ByteBuffer;

import com.ak.comm.core.AbstractService;

public abstract class AbstractBytesInterceptor<FROM, TO> extends AbstractService<FROM> implements BytesInterceptor<FROM, TO> {
  private final ByteBuffer outBuffer;
  private final TO startCommand;

  public AbstractBytesInterceptor(int outBufferSize, TO startCommand) {
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

  @Override
  public final ByteBuffer put(TO to) {
    outBuffer.clear();
    innerPut(outBuffer, to);
    outBuffer.flip();
    return outBuffer;
  }

  protected abstract void innerPut(ByteBuffer outBuffer, TO to);
}
