package com.ak.comm.core;

import java.nio.ByteBuffer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface Readable extends AutoCloseable {
  void read(@Nonnull ByteBuffer dst, @Nonnegative long position);

  /**
   * Empty implementation to remove Exception inherited from AutoCloseable.
   */
  @Override
  default void close() {
  }
}
