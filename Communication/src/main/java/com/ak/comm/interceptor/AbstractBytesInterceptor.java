package com.ak.comm.interceptor;

import java.nio.ByteBuffer;

import com.ak.comm.core.AbstractService;

public abstract class AbstractBytesInterceptor<RESPONSE, REQUEST> extends AbstractService<RESPONSE> implements BytesInterceptor<RESPONSE, REQUEST> {
  private final ByteBuffer outBuffer;
  private final REQUEST pingRequest;

  public AbstractBytesInterceptor(int outBufferSize, REQUEST pingRequest) {
    outBuffer = ByteBuffer.allocate(outBufferSize);
    this.pingRequest = pingRequest;
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
  public final REQUEST getPingRequest() {
    return pingRequest;
  }

  @Override
  public final ByteBuffer put(REQUEST request) {
    outBuffer.clear();
    innerPut(outBuffer, request);
    outBuffer.flip();
    return outBuffer;
  }

  protected abstract void innerPut(ByteBuffer outBuffer, REQUEST request);
}
