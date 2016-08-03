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

  @Nonnegative
  @Override
  public double getDelay() {
    return first.getDelay() + second.getDelay();
  }

  @Override
  public void accept(int... in) {
    first.accept(in);
  }

  @Nonnegative
  @Override
  public int size() {
    return second.size();
  }
}
