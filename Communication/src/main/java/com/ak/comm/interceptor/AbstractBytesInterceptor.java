package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import static jssc.SerialPort.BAUDRATE_115200;

public abstract class AbstractBytesInterceptor<RESPONSE, REQUEST> implements BytesInterceptor<RESPONSE, REQUEST> {
  private static final Level LOG_LEVEL_LEXEMES = Level.FINER;
  @Nonnull
  private final String name;
  private final ByteBuffer outBuffer;
  private final REQUEST pingRequest;
  private final Logger logger = Logger.getLogger(getClass().getName());

  protected AbstractBytesInterceptor(@Nonnull String name, @Nonnegative int outBufferSize, @Nullable REQUEST pingRequest) {
    this.name = name;
    outBuffer = ByteBuffer.allocate(outBufferSize);
    this.pingRequest = pingRequest;
  }

  @Override
  public final String name() {
    return name;
  }

  @Override
  public final Publisher<RESPONSE> apply(@Nonnull ByteBuffer src) {
    Flowable<RESPONSE> responses = innerProcessIn(src);
    if (logger.isLoggable(LOG_LEVEL_LEXEMES)) {
      responses.forEach(response -> logger.log(LOG_LEVEL_LEXEMES, String.format("#%x %s", hashCode(), response)));
    }
    return responses;
  }

  @Nullable
  @Override
  public final REQUEST getPingRequest() {
    return pingRequest;
  }

  @Override
  public final ByteBuffer putOut(@Nonnull REQUEST request) {
    logger.log(LOG_LEVEL_LEXEMES, String.format("#%x %s OUT to hardware", hashCode(), request));
    outBuffer.clear();
    innerPutOut(outBuffer, request);
    outBuffer.flip();
    return outBuffer;
  }

  @Nonnegative
  @Override
  public int getBaudRate() {
    return BAUDRATE_115200;
  }

  protected abstract void innerPutOut(@Nonnull ByteBuffer outBuffer, @Nonnull REQUEST request);

  protected abstract Flowable<RESPONSE> innerProcessIn(@Nonnull ByteBuffer src);
}
