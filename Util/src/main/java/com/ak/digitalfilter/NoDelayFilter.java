package com.ak.digitalfilter;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.ak.util.Strings.NEW_LINE;

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

  @Nonnegative
  @Override
  public double getFrequencyFactor() {
    return filter.getFrequencyFactor();
  }

  @Override
  public void accept(int... in) {
    filter.accept(in);
  }

  @Override
  public int size() {
    return filter.size();
  }

  @Override
  public String toString() {
    String base = String.format("%s (compensate %.1f delay) - ", getClass().getSimpleName(), filter.getDelay());
    return base + filter.toString().replaceAll(NEW_LINE, newLineTabSpaces(base.length()));
  }
}
