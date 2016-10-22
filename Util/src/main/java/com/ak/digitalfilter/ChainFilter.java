package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

import static com.ak.util.Strings.NEW_LINE;

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
    return second.getDelay(first.getDelay());
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

  @Override
  public String toString() {
    String base = String.format("%s - ", first.toString());
    return base + second.toString().replaceAll(NEW_LINE, newLineTabSpaces(base.length()));
  }
}
