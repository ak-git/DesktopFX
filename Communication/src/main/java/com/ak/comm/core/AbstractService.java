package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.comm.util.LogUtils;
import com.ak.util.FinalizerGuardian;

import static com.ak.comm.util.LogUtils.LOG_LEVEL_BYTES;

public abstract class AbstractService implements AutoCloseable {
  private final Logger logger = Logger.getLogger(getClass().getName());
  private final Object finalizerGuardian = new FinalizerGuardian(this);

  protected final void logBytes(@Nonnull ByteBuffer buffer) {
    LogUtils.logBytes(logger, LOG_LEVEL_BYTES, this, buffer, "IN from hardware");
  }
}