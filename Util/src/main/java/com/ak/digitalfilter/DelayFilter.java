package com.ak.digitalfilter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.ak.util.Strings.NEW_LINE;

final class DelayFilter extends AbstractDigitalFilter {
  @Nonnull
  private final DigitalFilter filter;
  @Nonnull
  private final int[][] buffer;
  private int bufferIndex = -1;

  DelayFilter(@Nonnull DigitalFilter filter, @Nonnegative int delayInSamples) {
    this.filter = filter;
    filter.forEach(this::publish);
    buffer = new int[delayInSamples][0];
  }

  @Override
  public int size() {
    return filter.size();
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
      buffer[bufferIndex] = new int[values.length];
    }
    filter.accept(buffer[bufferIndex]);
    buffer[bufferIndex] = values;
  }

  @Override
  public String toString() {
    String base = String.format("%s (delay %d) - ", getClass().getSimpleName(), buffer.length);
    return base + filter.toString().replaceAll(NEW_LINE, newLineTabSpaces(base.length()));
  }
}
