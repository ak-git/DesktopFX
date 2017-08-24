package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public enum EmptyByteChannel implements ByteChannel {
  INSTANCE;

  @Nonnegative
  @Override
  public int read(@Nonnull ByteBuffer dst) {
    return 0;
  }

  @Nonnegative
  @Override
  public int write(@Nonnull ByteBuffer src) {
    return 0;
  }

  @Override
  public boolean isOpen() {
    return false;
  }

  @Override
  public void close() {
  }
}
