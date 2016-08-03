package com.ak.digitalfilter;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class NoDelayFilter extends AbstractDigitalFilter {
  private final DigitalFilter filter;

  NoDelayFilter(@Nonnull DigitalFilter filter) {
    this.filter = filter;
    AtomicInteger skipSamples = new AtomicInteger((int) Math.round(filter.getDelay()));
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
  public double getDelay() {
    return 0.0;
  }

  @Override
  public void accept(int... in) {
    filter.accept(in);
  }

  @Override
  public int size() {
    return filter.size();
  }
}
