package com.ak.comm.core;

import java.nio.ByteBuffer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface Readable extends AutoCloseable {
  Readable EMPTY_READABLE = (dst, position) -> {
  };

  void read(@Nonnull ByteBuffer dst, @Nonnegative long position);

  @Override
  default void close() {
  }
}
