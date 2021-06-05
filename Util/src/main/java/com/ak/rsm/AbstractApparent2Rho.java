package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractApparent2Rho extends AbstractApparentRho {
  AbstractApparent2Rho(@Nonnull ResistanceSumValue apparent) {
    super(apparent);
  }

  final double value(double k, @Nonnegative double hToL) {
    if (Double.compare(k, 0.0) == 0 || Double.compare(hToL, 0.0) == 0) {
      return value(hToL, value -> 0.0);
    }
    else {
      return value(hToL, n -> kFactor(k, n));
    }
  }

  double kFactor(double k, @Nonnegative int n) {
    return StrictMath.pow(k, n) * sumFactor(n);
  }
}
