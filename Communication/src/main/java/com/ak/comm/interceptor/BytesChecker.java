package com.ak.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

public interface BytesChecker {
  default boolean is(byte b) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  default void buffer(@Nonnull ByteBuffer buffer) {
  }
}
