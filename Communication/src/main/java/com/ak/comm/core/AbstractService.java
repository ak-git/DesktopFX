package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.AbstractBufferFrame;
import com.ak.util.FinalizerGuardian;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import static com.ak.comm.core.LogLevels.LOG_LEVEL_BYTES;

public abstract class AbstractService<T> implements Publisher<T>, Subscription {
  private final Logger logger = Logger.getLogger(getClass().getName());
  private final Object finalizerGuardian = new FinalizerGuardian(this::cancel);

  protected final void logBytes(@Nonnull ByteBuffer buffer) {
    if (logger.isLoggable(LOG_LEVEL_BYTES)) {
      logger.log(LOG_LEVEL_BYTES, String.format("#%x %s IN from hardware", hashCode(), AbstractBufferFrame.toString(getClass(), buffer)));
    }
  }
}