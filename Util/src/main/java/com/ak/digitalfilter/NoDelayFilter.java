package com.ak.digitalfilter;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class NoDelayFilter extends AbstractDigitalFilter {
  @Nonnull
  private final DigitalFilter filter;

  NoDelayFilter(@Nonnull DigitalFilter filter) {
    this.filter = filter;
    AtomicInteger skipSamples = new AtomicInteger((int) Math.floor(filter.getDelay()) * 2);
    filter.forEach(values -> {
      if (skipSamples.get() == 0) {
        publish(values);
      }
      else {
        skipSamples.decrementAndGet();
      }
    });
  }

  @Nonnegative
  @Override
  public double getFrequencyFactor() {
    return filter.getFrequencyFactor();
  }

  @Override
  public double getDelay() {
    return -Math.floor(filter.getDelay());
  }

  @Override
  public void accept(int... in) {
    filter.accept(in);
  }

  @Override
  public int getOutputDataSize() {
    return filter.getOutputDataSize();
  }

  @Override
  public String toString() {
    return toString(String.format("%s (compensate %.1f delay x 2) - ", getClass().getSimpleName(), filter.getDelay()), filter);
  }
}
