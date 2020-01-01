package com.ak.comm.core;

import java.nio.ByteBuffer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface Readable extends AutoCloseable {
  void read(@Nonnull ByteBuffer dst, @Nonnegative long position);

  @Override
  default void close() {
  }
}
