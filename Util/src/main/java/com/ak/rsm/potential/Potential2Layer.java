package com.ak.rsm.potential;

import static java.lang.StrictMath.hypot;

public final class Potential2Layer extends AbstractPotentialLayer {
  public Potential2Layer(double r) {
    super(r);
  }

  public double value(int n, double h) {
    return value(r -> 1.0 / hypot(r, 2.0 * n * h));
  }
}
