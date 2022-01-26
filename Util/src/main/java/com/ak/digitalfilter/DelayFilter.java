package com.ak.digitalfilter;

import java.util.Arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class DelayFilter extends AbstractDigitalFilter {
  private static final int[] EMPTY_INTS = {};
  @Nonnull
  private final DigitalFilter filter;
  @Nonnull
  private final int[][] buffer;
  private int bufferIndex = -1;

  DelayFilter(@Nonnull DigitalFilter filter, @Nonnegative int delayInSamples) {
    filter.forEach(this::publish);
    this.filter = filter;
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

  @Nonnegative
  @Override
  public double getDelay() {
    return buffer.length + filter.getDelay();
  }

  @Override
  public double getFrequencyFactor() {
    return filter.getFrequencyFactor();
  }

  @Override
  public void accept(@Nonnull int... values) {
    bufferIndex = (++bufferIndex) % buffer.length;
    if (buffer[bufferIndex].length == 0) {
      Arrays.fill(buffer, values.clone());
    }
    filter.accept(buffer[bufferIndex]);
    buffer[bufferIndex] = values;
  }

  @Override
  public String toString() {
    return toString("%s (delay %d) - ".formatted(getClass().getSimpleName(), buffer.length), filter);
  }
}
