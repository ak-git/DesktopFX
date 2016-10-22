package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

abstract class AbstractBufferFilter extends AbstractOperableFilter {
  private final int[] buffer;
  private int bufferIndex = -1;

  AbstractBufferFilter(@Nonnegative int size) {
    buffer = new int[size];
  }

  @Nonnegative
  @Override
  public final double getDelay() {
    return (buffer.length - 1) / 2.0;
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

  abstract int apply(int nowIndex);
}