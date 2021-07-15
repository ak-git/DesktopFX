package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class Apparent3Rho extends AbstractApparentRho {
  Apparent3Rho(@Nonnull ResistanceSumValue apparent) {
    super(apparent);
  }

  double value(double k12, double k23, @Nonnegative double hToL, @Nonnegative int p1, @Nonnegative int p2mp1) {
    double[] qn = Layers.qn(k12, k23, p1, p2mp1);
    return value(hToL, n -> qn[n] * sumFactor(n));
  }

  static Apparent3Rho newDerivativeApparentByPhi3Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent3Rho(new DerivativeApparentByPhi(system));
  }

  static Apparent3Rho newLog1pApparent3Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent3Rho(new Log1pApparent(system));
  }
}
