package com.ak.rsm.potential;

import javax.annotation.Nonnegative;

import static java.lang.StrictMath.hypot;

public final class Potential3Layer extends AbstractPotentialLayer {
  @Nonnegative
  private final double hStep;

  public Potential3Layer(@Nonnegative double r, @Nonnegative double hStep) {
    super(r);
    this.hStep = Math.abs(hStep);
  }

  public double value(@Nonnegative int n) {
    return value(r -> 1.0 / hypot(r, 2.0 * n * hStep));
  }
}
