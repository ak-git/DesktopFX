package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.util.LogUtils;

import static com.ak.comm.util.LogUtils.LOG_LEVEL_ERRORS;
import static com.ak.comm.util.LogUtils.LOG_LEVEL_LEXEMES;

public abstract class AbstractBytesInterceptor<RESPONSE, REQUEST extends BufferFrame> implements BytesInterceptor<RESPONSE, REQUEST> {
  protected static final int IGNORE_LIMIT = 16;
  private final Logger logger = Logger.getLogger(getClass().getName());
  @Nonnull
  private final ByteBuffer outBuffer;
  @Nonnull
  private final ByteBuffer ignoreBuffer;
  @Nonnull
  private final BaudRate baudRate;
  @Nullable
  private final REQUEST pingRequest;

  public AbstractBytesInterceptor(@Nonnull BaudRate baudRate, @Nullable REQUEST pingRequest, int ignoreBufferLimit) {
    outBuffer = ByteBuffer.allocate(baudRate.get());
    ignoreBuffer = ByteBuffer.allocate(ignoreBufferLimit);
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
    request.writeTo(outBuffer);
    outBuffer.flip();
    if (logger.isLoggable(LOG_LEVEL_ERRORS)) {
      if (outBuffer.limit() > 1) {
        logger.log(LOG_LEVEL_ERRORS, String.format("#%x %s - %d bytes OUT to hardware", hashCode(), request, outBuffer.limit()));
      }
      else {
        logger.log(LOG_LEVEL_ERRORS, String.format("#%x %s - OUT to hardware", hashCode(), request));
      }
    }
    return outBuffer;
  }

  @Nonnegative
  @Override
  public int getBaudRate() {
    return baudRate.get();
  }

  @Nonnull
  protected abstract Collection<RESPONSE> innerProcessIn(@Nonnull ByteBuffer src);

  protected final ByteBuffer ignoreBuffer() {
    return ignoreBuffer;
  }

  protected final void logSkippedBytes(boolean force) {
    if (force || ignoreBuffer.position() >= IGNORE_LIMIT) {
      ignoreBuffer.flip();
      if (ignoreBuffer.limit() > 0) {
        LogUtils.logBytes(logger, LOG_LEVEL_ERRORS, this, ignoreBuffer, "IGNORED");
      }
      ignoreBuffer.clear();
    }
  }
}
