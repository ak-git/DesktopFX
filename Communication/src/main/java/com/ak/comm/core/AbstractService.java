package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.util.FinalizerGuardian;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import static com.ak.comm.core.LogLevels.LOG_LEVEL_BYTES;

public abstract class AbstractService<T> implements Publisher<T>, Subscription {
  private final Logger logger = Logger.getLogger(getClass().getName());
  private final Object finalizerGuardian = new FinalizerGuardian(this::cancel);

  public static void logBytes(@Nonnull Logger logger, @Nonnull Level level, @Nonnull Object aThis, @Nonnull ByteBuffer buffer,
                              @Nonnull String message) {
    if (logger.isLoggable(level)) {
      logger.log(level, String.format("#%x %s %s", aThis.hashCode(), BufferFrame.toString(aThis.getClass(), buffer), message));
    }
  }

  protected final void logBytes(@Nonnull ByteBuffer buffer) {
    logBytes(logger, LOG_LEVEL_BYTES, this, buffer, "IN from hardware");
  }
}