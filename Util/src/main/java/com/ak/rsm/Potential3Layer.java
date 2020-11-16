package com.ak.rsm;

import javax.annotation.Nonnegative;

import static java.lang.StrictMath.hypot;

final class Potential3Layer extends AbstractPotentialLayer {
  @Nonnegative
  private final double hStep;

  Potential3Layer(double r, double hStep) {
    super(r);
    this.hStep = Math.abs(hStep);
  }

  double value(@Nonnegative int n) {
    return value(r -> 1.0 / hypot(r, 2.0 * n * hStep));
  }
}
