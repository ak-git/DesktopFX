package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.util.concurrent.Flow;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.LogUtils;
import com.ak.comm.converter.Refreshable;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_BYTES;

public abstract class AbstractService<F> implements Flow.Publisher<F>, Refreshable {
  private final Logger logger = Logger.getLogger(getClass().getName());

  protected final void logBytes(@Nonnull ByteBuffer buffer) {
    LogUtils.logBytes(logger, LOG_LEVEL_BYTES, this, buffer, "IN from hardware");
  }
}