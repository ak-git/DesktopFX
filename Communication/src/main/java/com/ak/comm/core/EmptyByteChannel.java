package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class EmptyByteChannel implements SeekableByteChannel {
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

  @Nonnegative
  @Override
  public long position() {
    return 0;
  }

  @Override
  public SeekableByteChannel position(@Nonnegative long newPosition) {
    return this;
  }

  @Nonnegative
  @Override
  public long size() {
    return 0;
  }

  @Override
  public SeekableByteChannel truncate(@Nonnegative long size) {
    return this;
  }

  @Override
  public boolean isOpen() {
    return false;
  }

  @Override
  public void close() {
  }
}
