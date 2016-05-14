package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.comm.core.AbstractService;

public abstract class AbstractBytesInterceptor<RESPONSE, REQUEST> extends AbstractService<RESPONSE> implements BytesInterceptor<RESPONSE, REQUEST> {
  private final String name;
  private final ByteBuffer outBuffer;
  private final REQUEST pingRequest;

  protected AbstractBytesInterceptor(String name, int outBufferSize, REQUEST pingRequest) {
    this.name = name;
    outBuffer = ByteBuffer.allocate(outBufferSize);
    this.pingRequest = pingRequest;
    bufferPublish().subscribe(response -> Logger.getLogger(getClass().getName()).log(Level.CONFIG,
        String.format("#%x %s", hashCode(), response)));
  }

  @Override
  public final String name() {
    return name;
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
    Logger.getLogger(getClass().getName()).log(Level.CONFIG, String.format("#%x %s", hashCode(), request));
    outBuffer.clear();
    innerPut(outBuffer, request);
    outBuffer.flip();
    return outBuffer;
  }

  @Override
  public int getBaudRate() {
    return 115200;
  }

  protected abstract void innerPut(ByteBuffer outBuffer, REQUEST request);
}
