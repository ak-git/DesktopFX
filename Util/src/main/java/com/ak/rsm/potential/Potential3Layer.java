package com.ak.rsm.potential;

import static java.lang.StrictMath.hypot;

public final class Potential3Layer extends AbstractPotentialLayer {
  private final double hStep;

  public Potential3Layer(double r, double hStep) {
    super(r);
    this.hStep = Math.abs(hStep);
  }

  public double value(int n) {
    return value(r -> 1.0 / hypot(r, 2.0 * n * hStep));
  }
}
