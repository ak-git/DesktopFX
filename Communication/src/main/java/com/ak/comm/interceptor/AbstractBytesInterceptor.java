package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.ak.comm.core.LogLevels.LOG_LEVEL_LEXEMES;

public abstract class AbstractBytesInterceptor<RESPONSE, REQUEST> implements BytesInterceptor<RESPONSE, REQUEST> {
  private final Logger logger = Logger.getLogger(getClass().getName());
  @Nonnull
  private final ByteBuffer outBuffer;
  @Nonnull
  private final BaudRate baudRate;
  @Nullable
  private final REQUEST pingRequest;

  protected AbstractBytesInterceptor(@Nonnull BaudRate baudRate, @Nullable REQUEST pingRequest) {
    outBuffer = ByteBuffer.allocate(baudRate.get());
    this.baudRate = baudRate;
    this.pingRequest = pingRequest;
  }

  @Override
  public final Stream<RESPONSE> apply(@Nonnull ByteBuffer src) {
    Collection<RESPONSE> responses = innerProcessIn(src);
    if (logger.isLoggable(LOG_LEVEL_LEXEMES)) {
      responses.forEach(response -> logger.log(LOG_LEVEL_LEXEMES, String.format("#%x %s", hashCode(), response)));
    }
    return responses.stream();
  }

  @Nullable
  @Override
  public final REQUEST getPingRequest() {
    return pingRequest;
  }

  @Override
  public final ByteBuffer putOut(@Nonnull REQUEST request) {
    outBuffer.clear();
    innerPutOut(outBuffer, request);
    outBuffer.flip();
    if (logger.isLoggable(LOG_LEVEL_LEXEMES)) {
      if (outBuffer.limit() > 1) {
        logger.log(LOG_LEVEL_LEXEMES, String.format("#%x %s - %d bytes OUT to hardware", hashCode(), request, outBuffer.limit()));
      }
      else {
        logger.log(LOG_LEVEL_LEXEMES, String.format("#%x %s - OUT to hardware", hashCode(), request));
      }
    }
    return outBuffer;
  }

  @Nonnegative
  @Override
  public int getBaudRate() {
    return baudRate.get();
  }

  protected abstract void innerPutOut(@Nonnull ByteBuffer outBuffer, @Nonnull REQUEST request);

  @Nonnull
  protected abstract Collection<RESPONSE> innerProcessIn(@Nonnull ByteBuffer src);
}
