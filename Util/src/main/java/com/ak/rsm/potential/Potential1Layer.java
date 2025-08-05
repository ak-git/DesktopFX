package com.ak.rsm.potential;

public final class Potential1Layer extends AbstractPotentialLayer {
  public Potential1Layer(double r) {
    super(r);
  }

  public double value() {
    return value(r -> 1.0 / r);
  }
}
