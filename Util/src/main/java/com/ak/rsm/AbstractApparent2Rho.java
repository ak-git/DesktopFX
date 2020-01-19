package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractApparent2Rho extends AbstractApparentRho {
  AbstractApparent2Rho(@Nonnull Apparent apparent) {
    super(apparent);
  }

  final double value(double k, @Nonnegative double h) {
    return value(h, n -> StrictMath.pow(k, n) * sumFactor(n));
  }
}
