package com.ak.rsm.potential;

import javax.annotation.Nonnegative;

public final class Potential1Layer extends AbstractPotentialLayer {
  public Potential1Layer(@Nonnegative double r) {
    super(r);
  }

  public double value() {
    return value(r -> 1.0 / r);
  }
}
