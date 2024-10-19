package com.ak.digitalfilter;

import javax.annotation.Nonnegative;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

final class NoDelayFilter extends AbstractDigitalFilter {
  private final DigitalFilter filter;
  private final AtomicInteger skipSamples;

  NoDelayFilter(DigitalFilter filter) {
    this.filter = Objects.requireNonNull(filter);
    skipSamples = new AtomicInteger((int) Math.round(filter.getDelay() * 2));
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
  public void reset() {
    filter.reset();
    skipSamples.set((int) Math.round(filter.getDelay() * 2));
  }

  @Override
  public int getOutputDataSize() {
    return filter.getOutputDataSize();
  }

  @Override
  public String toString() {
    return toString("%s (compensate %.1f delay x 2) - ".formatted(getClass().getSimpleName(), filter.getDelay()), filter);
  }
}
