package com.ak.comm.bytes;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

public interface BytesChecker {
  default boolean is(byte b) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  default void bufferLimit(@Nonnull ByteBuffer buffer) {
    //Default implementation does nothing with buffer.
  }

  default boolean isCheckedAndLimitSet(byte b, @Nonnull ByteBuffer buffer) {
    boolean check = is(b);
    if (check) {
      bufferLimit(buffer);
    }
    return check;
  }
}
