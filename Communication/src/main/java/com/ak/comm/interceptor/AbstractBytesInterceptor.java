package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.core.AbstractService;

import static jssc.SerialPort.BAUDRATE_115200;

public abstract class AbstractBytesInterceptor<RESPONSE, REQUEST> extends AbstractService<RESPONSE> implements BytesInterceptor<RESPONSE, REQUEST> {
  private static final Level LOG_LEVEL = Level.FINER;
  private final String name;
  private final ByteBuffer outBuffer;
  private final REQUEST pingRequest;
  private final Logger logger = Logger.getLogger(getClass().getName());

  protected AbstractBytesInterceptor(@Nonnull String name, @Nonnegative int outBufferSize, @Nullable REQUEST pingRequest) {
    this.name = name;
    outBuffer = ByteBuffer.allocate(outBufferSize);
    this.pingRequest = pingRequest;
    if (logger.isLoggable(LOG_LEVEL)) {
      bufferPublish().subscribe(response -> logger.log(LOG_LEVEL, String.format("#%x %s", hashCode(), response)));
    }
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
    logger.log(LOG_LEVEL, String.format("#%x %s", hashCode(), request));
    outBuffer.clear();
    innerPut(outBuffer, request);
    outBuffer.flip();
    return outBuffer;
  }

  @Nonnegative
  @Override
  public int getBaudRate() {
    return BAUDRATE_115200;
  }

  protected abstract void innerPut(@Nonnull ByteBuffer outBuffer, @Nonnull REQUEST request);
}
