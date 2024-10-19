package com.ak.comm.bytes;

import java.nio.ByteBuffer;
import java.util.Objects;

public interface BytesChecker {
  default boolean is(byte b) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  default void bufferLimit(ByteBuffer buffer) {
    //Default implementation does nothing with buffer.
  }

  default boolean isCheckedAndLimitSet(byte b, ByteBuffer buffer) {
    boolean check = is(b);
    if (check) {
      bufferLimit(Objects.requireNonNull(buffer));
    }
    return check;
  }
}
