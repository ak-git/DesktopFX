package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.ak.comm.core.AbstractService;

import static jssc.SerialPort.BAUDRATE_115200;

public abstract class AbstractBytesInterceptor<RESPONSE, REQUEST> extends AbstractService<RESPONSE> implements BytesInterceptor<RESPONSE, REQUEST> {
  private static final Level LOG_LEVEL_LEXEMES = Level.FINER;
  private static final Level LOG_LEVEL_BYTES = Level.FINEST;
  private final String name;
  private final ByteBuffer outBuffer;
  private final REQUEST pingRequest;
  private final Logger logger = Logger.getLogger(getClass().getName());

  protected AbstractBytesInterceptor(@Nonnull String name, @Nonnegative int outBufferSize, @Nullable REQUEST pingRequest) {
    this.name = name;
    outBuffer = ByteBuffer.allocate(outBufferSize);
    this.pingRequest = pingRequest;
    if (logger.isLoggable(LOG_LEVEL_LEXEMES)) {
      bufferPublish().subscribe(response -> logger.log(LOG_LEVEL_LEXEMES, String.format("#%x %s", hashCode(), response)));
    }
  }

  @Nonnull
  @Override
  public final String name() {
    return name;
  }

  @OverridingMethodsMustInvokeSuper
  @Nonnegative
  @Override
  public int write(@Nonnull ByteBuffer src) {
    if (logger.isLoggable(LOG_LEVEL_BYTES)) {
      logger.log(LOG_LEVEL_BYTES, String.format("#%x %s IN from hardware", hashCode(), AbstractBufferFrame.toString(getClass(), src)));
    }
    return 0;
  }

  @Nullable
  @Override
  public final REQUEST getPingRequest() {
    return pingRequest;
  }

  @Nonnull
  @Override
  public final ByteBuffer putOut(@Nonnull REQUEST request) {
    logger.log(LOG_LEVEL_LEXEMES, String.format("#%x %s OUT to hardware", hashCode(), request));
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
