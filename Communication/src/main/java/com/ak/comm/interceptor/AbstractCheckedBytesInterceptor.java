package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.AbstractBufferFrame;
import com.ak.comm.bytes.AbstractCheckedBuilder;
import io.reactivex.Flowable;

public abstract class AbstractCheckedBytesInterceptor<B extends AbstractCheckedBuilder<RESPONSE>, RESPONSE, REQUEST extends AbstractBufferFrame>
    extends AbstractBytesInterceptor<RESPONSE, REQUEST> {
  private static final Level LOG_LEVEL_ERRORS = Level.CONFIG;
  private static final int IGNORE_LIMIT = 16;
  private final Logger logger = Logger.getLogger(getClass().getName());
  private final ByteBuffer ignoreBuffer;

  @Nonnull
  private final B responseBuilder;

  protected AbstractCheckedBytesInterceptor(@Nonnull String name, @Nullable REQUEST pingRequest,
                                            @Nonnull B responseBuilder) {
    super(name, responseBuilder.buffer().capacity(), pingRequest);
    this.responseBuilder = responseBuilder;
    ignoreBuffer = ByteBuffer.allocate(responseBuilder.buffer().capacity() + IGNORE_LIMIT);
  }

  @Override
  protected final void innerPutOut(@Nonnull ByteBuffer outBuffer, @Nonnull REQUEST request) {
    request.writeTo(outBuffer);
  }

  @Override
  protected Flowable<RESPONSE> innerProcessIn(@Nonnull ByteBuffer src) {
    src.rewind();
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
          logger.log(LOG_LEVEL_ERRORS, String.format("#%x %s INVALID FRAME", hashCode(), AbstractBufferFrame.toString(getClass(), buffer)));
        }
        else {
          responses.add(response);
        }
        buffer.clear();
      }
    }
    return Flowable.fromIterable(responses);
  }

  private void logSkippedBytes() {
    ignoreBuffer.flip();
    if (ignoreBuffer.limit() > 0) {
      logger.log(LOG_LEVEL_ERRORS, String.format("#%x %s IGNORED", hashCode(), AbstractBufferFrame.toString(getClass(), ignoreBuffer)));
    }
    ignoreBuffer.clear();
  }
}
