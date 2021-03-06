package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractApparent3Rho extends AbstractApparentRho {
  AbstractApparent3Rho(@Nonnull ResistanceSumValue apparent) {
    super(apparent);
  }

  final double value(double k12, double k23, @Nonnegative double hToL, @Nonnegative int p1, @Nonnegative int p2mp1) {
    double[] qn = Layers.qn(k12, k23, p1, p2mp1);
    return value(hToL, n -> qn[n] * sumFactor(n));
  }
}
