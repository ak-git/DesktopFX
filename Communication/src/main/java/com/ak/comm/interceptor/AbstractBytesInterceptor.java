package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.core.LogUtils;

import static com.ak.comm.core.LogUtils.LOG_LEVEL_ERRORS;
import static com.ak.comm.core.LogUtils.LOG_LEVEL_LEXEMES;

public abstract class AbstractBytesInterceptor<T extends BufferFrame, R> implements BytesInterceptor<T, R> {
  protected static final int IGNORE_LIMIT = 16;
  private final Logger logger = Logger.getLogger(getClass().getName());
  @Nonnull
  private final String name;
  @Nonnull
  private final ByteBuffer outBuffer;
  @Nonnull
  private final ByteBuffer ignoreBuffer;
  @Nonnull
  private final BaudRate baudRate;
  @Nullable
  private final T pingRequest;

  protected AbstractBytesInterceptor(@Nonnull String name, @Nonnull BaudRate baudRate, @Nullable T pingRequest, int ignoreBufferLimit) {
    this.name = name;
    outBuffer = ByteBuffer.allocate(baudRate.get());
    ignoreBuffer = ByteBuffer.allocate(ignoreBufferLimit);
    this.baudRate = baudRate;
    this.pingRequest = pingRequest;
  }

  @Override
  public final Stream<R> apply(@Nonnull ByteBuffer src) {
    Collection<R> responses = innerProcessIn(src);
    if (logger.isLoggable(LOG_LEVEL_LEXEMES)) {
      responses.forEach(response -> logger.log(LOG_LEVEL_LEXEMES, "#%08x %s".formatted(hashCode(), response)));
    }
    return responses.stream();
  }

  @Nullable
  @Override
  public final T getPingRequest() {
    return pingRequest;
  }

  @Override
  public final ByteBuffer putOut(@Nonnull T request) {
    outBuffer.clear();
    request.writeTo(outBuffer);
    outBuffer.flip();
    if (logger.isLoggable(LOG_LEVEL_ERRORS)) {
      if (outBuffer.limit() > 1) {
        logger.log(LOG_LEVEL_ERRORS, "#%08x %s - %d bytes OUT to hardware".formatted(hashCode(), request, outBuffer.limit()));
      }
      else {
        logger.log(LOG_LEVEL_ERRORS, "#%08x %s - OUT to hardware".formatted(hashCode(), request));
      }
    }
    return outBuffer;
  }

  @Nonnull
  @Override
  public final String name() {
    return name;
  }

  @Nonnegative
  @Override
  public int getBaudRate() {
    return baudRate.get();
  }

  @Nonnull
  protected abstract Collection<R> innerProcessIn(@Nonnull ByteBuffer src);

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
