package com.ak.rsm.apparent;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;

public class Apparent3Rho extends AbstractApparentRho {
  private Apparent3Rho(@Nonnull ResistanceSumValue apparent) {
    super(apparent);
  }

  public double value(double k12, double k23, @Nonnegative double hToL, @Nonnegative int p1, @Nonnegative int p2mp1) {
    double[] qn = Layers.qn(k12, k23, p1, p2mp1);
    return value(hToL, n -> qn[n] * sumFactor(n));
  }

  public static Apparent3Rho newLog1pApparent3Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent3Rho(new Log1pApparent(system));
  }

  public static Apparent3Rho newDerivativeApparentByPhi2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent3Rho(new DerivativeApparentByPhi(system));
  }
}
