package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class DelayFilter extends AbstractOperableFilter {
  private final int[] buffer;
  private int bufferIndex = -1;

  DelayFilter(@Nonnegative int delayInSamples) {
    buffer = new int[delayInSamples];
  }

  @Nonnegative
  @Override
  public double getDelay() {
    return buffer.length;
  }

  @Override
  public int applyAsInt(int in) {
    bufferIndex = (++bufferIndex) % buffer.length;
    int result = buffer[bufferIndex];
    buffer[bufferIndex] = in;
    return result;
  }
}
