package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractApparent2Rho extends AbstractApparentRho {
  AbstractApparent2Rho(@Nonnull ResistanceSumValue apparent) {
    super(apparent);
  }

  final double value(double k, @Nonnegative double h) {
    if (Double.compare(k, 0.0) == 0 || Double.compare(h, 0.0) == 0) {
      return value(h, value -> 0.0);
    }
    else {
      return value(h, n -> StrictMath.pow(k, n) * sumFactor(n));
    }
  }
}
