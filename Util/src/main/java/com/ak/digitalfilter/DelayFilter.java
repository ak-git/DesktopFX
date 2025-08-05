package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.Objects;

final class DelayFilter extends AbstractDigitalFilter {
  private final DigitalFilter filter;
  private final int[][] buffer;
  private int bufferIndex = -1;

  DelayFilter(DigitalFilter filter, int delayInSamples) {
    this.filter = Objects.requireNonNull(filter);
    filter.forEach(this::publish);
    buffer = new int[delayInSamples][0];
  }

  @Override
  public void reset() {
    filter.reset();
    Arrays.fill(buffer, EMPTY_INTS);
  }

  @Override
  public int getOutputDataSize() {
    return filter.getOutputDataSize();
  }

  @Override
  public double getDelay() {
    return buffer.length + filter.getDelay();
  }

  @Override
  public double getFrequencyFactor() {
    return filter.getFrequencyFactor();
  }

  @Override
  public void accept(int... values) {
    bufferIndex = (++bufferIndex) % buffer.length;
    if (buffer[bufferIndex].length == 0) {
      Arrays.fill(buffer, values.clone());
    }
    filter.accept(buffer[bufferIndex]);
    buffer[bufferIndex] = Objects.requireNonNull(values);
  }

  @Override
  public String toString() {
    return toString("%s (delay %d) - ".formatted(getClass().getSimpleName(), buffer.length), filter);
  }
}
