package com.ak.comm.core;

import java.nio.ByteBuffer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.converter.Refreshable;

@FunctionalInterface
public interface Readable extends Refreshable, AutoCloseable {
  void read(@Nonnull ByteBuffer dst, @Nonnegative long position);

  @Override
  default void refresh(boolean force) {
    close();
  }

  @Override
  default void close() {
    Refreshable.super.close();
  }
}
