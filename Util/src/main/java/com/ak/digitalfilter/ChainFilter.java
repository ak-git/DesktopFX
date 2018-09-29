package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class ChainFilter extends AbstractDigitalFilter {
  private final DigitalFilter first;
  private final DigitalFilter second;

  ChainFilter(DigitalFilter first, DigitalFilter second) {
    this.first = first;
    this.second = second;
    first.forEach(second);
    second.forEach(this::publish);
  }

  @Override
  public double getDelay() {
    return first.getDelay() * second.getFrequencyFactor() + second.getDelay();
  }

  @Nonnegative
  @Override
  public double getFrequencyFactor() {
    return first.getFrequencyFactor() * second.getFrequencyFactor();
  }

  @Override
  public void accept(int... in) {
    first.accept(in);
  }

  @Override
  public void reset() {
    first.reset();
    second.reset();
  }

  @Nonnegative
  @Override
  public int getOutputDataSize() {
    return second.getOutputDataSize();
  }

  @Override
  public String toString() {
    return toString(String.format("%s - ", first.toString()), second);
  }
}
