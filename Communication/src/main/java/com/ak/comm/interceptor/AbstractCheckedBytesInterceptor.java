package com.ak.comm.interceptor;

import com.ak.comm.bytes.AbstractCheckedBuilder;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.bytes.LogUtils;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_ERRORS;

public abstract class AbstractCheckedBytesInterceptor<T extends BufferFrame, R, B extends AbstractCheckedBuilder<R>>
    extends AbstractBytesInterceptor<T, R> {
  private final Logger logger = Logger.getLogger(getClass().getName());
  private final B responseBuilder;

  protected AbstractCheckedBytesInterceptor(String name, BaudRate baudRate, B responseBuilder, T pingRequest) {
    super(name, baudRate, responseBuilder.buffer().limit() + IGNORE_LIMIT, pingRequest);
    this.responseBuilder = responseBuilder;
  }

  @Override
  protected final Collection<R> innerProcessIn(ByteBuffer src) {
    Collection<R> responses = new LinkedList<>();
    ByteBuffer buffer = responseBuilder.buffer();
    while (src.hasRemaining()) {
      check(src.get(), buffer);

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

  private void check(byte b, ByteBuffer buffer) {
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
  }
}
