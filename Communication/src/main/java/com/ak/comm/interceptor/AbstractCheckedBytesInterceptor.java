package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.bytes.LogUtils;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_ERRORS;

public abstract class AbstractCheckedBytesInterceptor<T extends BufferFrame, R, B extends AbstractCheckedBuilder<R>>
    extends AbstractBytesInterceptor<T, R> {
  private final Logger logger = Logger.getLogger(getClass().getName());
  @Nonnull
  private final B responseBuilder;

  protected AbstractCheckedBytesInterceptor(@Nonnull String name, @Nonnull BaudRate baudRate,
                                            @Nullable T pingRequest, @Nonnull B responseBuilder) {
    super(name, baudRate, pingRequest, responseBuilder.buffer().limit() + IGNORE_LIMIT);
    this.responseBuilder = responseBuilder;
  }

  @Override
  protected final Collection<R> innerProcessIn(@Nonnull ByteBuffer src) {
    Collection<R> responses = new LinkedList<>();
    ByteBuffer buffer = responseBuilder.buffer();
    while (src.hasRemaining()) {
      byte b = src.get();

      for (var i = 0; i < 2; i++) {
        buffer.put(b);
        if (responseBuilder.is(b)) {
          if (i == 1) {
            ignoreBuffer().position(ignoreBuffer().position() - 1);
          }
          break;
        }
        else {
          buffer.flip().rewind();
          if (i == 0) {
            ignoreBuffer().put(buffer);
          }
          buffer.clear();
        }
      }

      logSkippedBytes(false);

      if (!buffer.hasRemaining()) {
        logSkippedBytes(true);

        var response = responseBuilder.build();
        if (response == null) {
          LogUtils.logBytes(logger, LOG_LEVEL_ERRORS, this, buffer, "INVALID FRAME");
        }
        else {
          responses.add(response);
        }
        buffer.clear();
      }
    }
    return responses;
  }
}
