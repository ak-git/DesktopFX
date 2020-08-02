package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.util.concurrent.Flow;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.comm.converter.Refreshable;
import com.ak.util.LogUtils;

import static com.ak.util.LogUtils.LOG_LEVEL_BYTES;

public abstract class AbstractService<FP> implements AutoCloseable, Flow.Publisher<FP>, Refreshable {
  private final Logger logger = Logger.getLogger(getClass().getName());

  protected final void logBytes(@Nonnull ByteBuffer buffer) {
    LogUtils.logBytes(logger, LOG_LEVEL_BYTES, this, buffer, "IN from hardware");
  }
}