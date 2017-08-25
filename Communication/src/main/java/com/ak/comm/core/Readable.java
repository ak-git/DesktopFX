package com.ak.comm.core;

import java.nio.ByteBuffer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface Readable {
  Readable EMPTY_READABLE = (dst, position) -> {
  };

  void read(@Nonnull ByteBuffer dst, @Nonnegative long position);
}
