package com.ak.comm.core;

import java.nio.ByteBuffer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface Readable {
  Readable EMPTY_READABLE = (dst, position) -> -1;

  /**
   * Read bytes into buffer starting from channel position.
   *
   * @param dst      Destination buffer
   * @param position inclusive channel position
   * @return total bytes available
   */
  long read(@Nonnull ByteBuffer dst, @Nonnegative long position);
}
