package com.ak.rsm;

import javax.annotation.Nonnegative;

import com.ak.util.Strings;

final class Layer2RelativeMedium implements RelativeMediumLayers {
  private final double k12;
  @Nonnegative
  private final double h;

  Layer2RelativeMedium(double k12, double h) {
    this.k12 = k12;
    this.h = h;
  }

  @Override
  public double k12() {
    return k12;
  }

  @Override
  public double h() {
    return h;
  }

  @Override
  public String toString() {
    return "k%s%s = %+.3f; %s".formatted(Strings.low(1), Strings.low(2), k12(), Strings.h(h(), 1));
  }
}
