package com.ak.comm.core;

import com.ak.comm.converter.Refreshable;

import javax.annotation.Nonnegative;
import java.nio.ByteBuffer;

@FunctionalInterface
public interface Readable extends Refreshable, AutoCloseable {
  void read(ByteBuffer dst, @Nonnegative long position);

  @Override
  default void refresh(boolean force) {
    close();
  }

  @Override
  default void close() {
    Refreshable.super.close();
  }
}
