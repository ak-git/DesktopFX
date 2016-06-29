package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.ak.comm.core.AbstractService;

@Immutable
public abstract class AbstractBytesInterceptor<RESPONSE, REQUEST> extends AbstractService<RESPONSE> implements BytesInterceptor<RESPONSE, REQUEST> {
  private final String name;
  private final ByteBuffer outBuffer;
  private final REQUEST pingRequest;

  protected AbstractBytesInterceptor(@Nonnull String name, @Nonnegative int outBufferSize, @Nullable REQUEST pingRequest) {
    this.name = name;
    outBuffer = ByteBuffer.allocate(outBufferSize);
    this.pingRequest = pingRequest;
    bufferPublish().subscribe(response -> Logger.getLogger(getClass().getName()).log(Level.CONFIG,
        String.format("#%x %s", hashCode(), response)));
  }

  @Nonnull
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

  @Nullable
  @Override
  public final REQUEST getPingRequest() {
    return pingRequest;
  }

  @Nonnull
  @Override
  public final ByteBuffer put(@Nonnull REQUEST request) {
    Logger.getLogger(getClass().getName()).log(Level.CONFIG, String.format("#%x %s", hashCode(), request));
    outBuffer.clear();
    innerPut(outBuffer, request);
    outBuffer.flip();
    return outBuffer;
  }

  @Nonnegative
  @Override
  public int getBaudRate() {
    return 115200;
  }

  protected abstract void innerPut(@Nonnull ByteBuffer outBuffer, @Nonnull REQUEST request);
}
