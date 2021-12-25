package com.ak.rsm.potential;

import javax.annotation.Nonnegative;

import static java.lang.StrictMath.hypot;

public final class Potential2Layer extends AbstractPotentialLayer {
  public Potential2Layer(@Nonnegative double r) {
    super(r);
  }

  public double value(@Nonnegative int n, @Nonnegative double h) {
    return value(r -> 1.0 / hypot(r, 2.0 * n * h));
  }
}
