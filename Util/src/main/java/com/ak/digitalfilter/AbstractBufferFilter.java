package com.ak.digitalfilter;

import java.util.Arrays;

import javax.annotation.Nonnegative;

abstract class AbstractBufferFilter extends AbstractOperableFilter {
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

  final int[] buffer() {
    return Arrays.copyOf(buffer, buffer.length);
  }

  final int length() {
    return buffer.length;
  }

  abstract int apply(@Nonnegative int nowIndex);
}