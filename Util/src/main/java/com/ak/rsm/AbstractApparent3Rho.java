package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractApparent3Rho extends AbstractApparentRho {
  AbstractApparent3Rho(@Nonnull ResistanceSumValue apparent) {
    super(apparent);
  }

  final double value(double k12, double k23, @Nonnegative double h, @Nonnegative int p1, @Nonnegative int p2mp1) {
    if (Double.compare(h, 0.0) == 0) {
      return value(h, value -> 0.0);
    }
    else {
      double[] qn = Layers.qn(k12, k23, p1, p2mp1);
      return value(h, n -> qn[n] * sumFactor(n));
    }
  }
}
