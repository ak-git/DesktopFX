package com.ak.digitalfilter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractBufferFilter extends AbstractOperableFilter {
  @Nonnull
  private final int[] buffer;
  private int bufferIndex = -1;

  AbstractBufferFilter(@Nonnegative int size) {
    buffer = new int[size];
  }

  @Nonnegative
  @Override
  public double getDelay() {
    return (length() - 1) / 2.0;
  }

  @Override
  public final int applyAsInt(int in) {
    bufferIndex = (++bufferIndex) % buffer.length;
    buffer[bufferIndex] = in;
    return apply(bufferIndex);
  }

  final int get(@Nonnegative int index) {
    return buffer[index % buffer.length];
  }

  @Nonnull
  final int[] buffer() {
    return buffer.clone();
  }

  @Nonnegative
  final int length() {
    return buffer.length;
  }

  abstract int apply(@Nonnegative int nowIndex);
}