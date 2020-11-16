package com.ak.comm.core;

import java.nio.ByteBuffer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.converter.Refreshable;

@FunctionalInterface
public interface Readable extends AutoCloseable, Refreshable {
  void read(@Nonnull ByteBuffer dst, @Nonnegative long position);

  @Override
  default void close() {
    //Empty implementation to remove Exception inherited from AutoCloseable.
  }

  @Override
  default void refresh() {
    close();
  }
}
