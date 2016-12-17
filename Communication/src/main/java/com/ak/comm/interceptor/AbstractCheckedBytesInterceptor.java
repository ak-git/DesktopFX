package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.AbstractBufferFrame;
import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.core.AbstractService;

import static com.ak.comm.core.LogLevels.LOG_LEVEL_ERRORS;

public abstract class AbstractCheckedBytesInterceptor<B extends AbstractCheckedBuilder<RESPONSE>, RESPONSE, REQUEST extends AbstractBufferFrame>
    extends AbstractBytesInterceptor<RESPONSE, REQUEST> {
  private static final int IGNORE_LIMIT = 16;
  private final Logger logger = Logger.getLogger(getClass().getName());
  private final ByteBuffer ignoreBuffer;

  @Nonnull
  private final B responseBuilder;

  protected AbstractCheckedBytesInterceptor(@Nonnull BaudRate baudRate, @Nullable REQUEST pingRequest, @Nonnull B responseBuilder) {
    super(baudRate, pingRequest);
    this.responseBuilder = responseBuilder;
    ignoreBuffer = ByteBuffer.allocate(responseBuilder.buffer().capacity() + IGNORE_LIMIT);
  }

  @Override
  protected final void innerPutOut(@Nonnull ByteBuffer outBuffer, @Nonnull REQUEST request) {
    request.writeTo(outBuffer);
  }

  @Override
  protected Collection<RESPONSE> innerProcessIn(@Nonnull ByteBuffer src) {
    Collection<RESPONSE> responses = new LinkedList<>();
    ByteBuffer buffer = responseBuilder.buffer();
    while (src.hasRemaining()) {
      byte b = src.get();

      for (int i = 0; i < 2; i++) {
        buffer.put(b);
        if (responseBuilder.is(b)) {
          if (i == 1) {
            ignoreBuffer.position(ignoreBuffer.position() - 1);
          }
          break;
        }
        else {
          buffer.flip().rewind();
          if (i == 0) {
            ignoreBuffer.put(buffer);
          }
          buffer.clear();
        }
      }

      if (ignoreBuffer.position() > IGNORE_LIMIT - 1) {
        logSkippedBytes();
      }

      if (!buffer.hasRemaining()) {
        logSkippedBytes();

        RESPONSE response = responseBuilder.build();
        if (response == null) {
          AbstractService.logBytes(logger, LOG_LEVEL_ERRORS, this, buffer, "INVALID FRAME");
        }
        else {
          responses.add(response);
        }
        buffer.clear();
      }
    }
    return responses;
  }

  private void logSkippedBytes() {
    ignoreBuffer.flip();
    if (ignoreBuffer.limit() > 0) {
      AbstractService.logBytes(logger, LOG_LEVEL_ERRORS, this, ignoreBuffer, "IGNORED");
    }
    ignoreBuffer.clear();
  }
}
