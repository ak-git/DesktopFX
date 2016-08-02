package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class DelayFilter extends AbstractDigitalFilter {
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
  public void accept(int in) {
    bufferIndex = (++bufferIndex) % buffer.length;
    publish(buffer[bufferIndex]);
    buffer[bufferIndex] = in;
  }
}
