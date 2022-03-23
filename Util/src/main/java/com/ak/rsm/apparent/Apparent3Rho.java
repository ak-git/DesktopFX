package com.ak.rsm.apparent;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;

public class Apparent3Rho extends AbstractApparentRho {
  private Apparent3Rho(@Nonnull ResistanceSumValue apparent) {
    super(apparent);
  }

  public double value(double k12, double k23, @Nonnegative double hToL, @Nonnegative int p1, @Nonnegative int p2mp1) {
    double[] qn = Layers.qn(k12, k23, p1, p2mp1);
    return value(hToL, n -> qn[n] * sumFactor(n));
  }

  @Nonnull
  public static Apparent3Rho newLog1pApparent3Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent3Rho(new Log1pApparent(system));
  }

  @Nonnull
  public static Apparent3Rho newNormalizedApparent2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent3Rho(new NormalizedApparent(system));
  }

  @ParametersAreNonnullByDefault
  public static double newDerivativeApparentByPhi2Rho(TetrapolarSystem s, double[] k, double hStep,
                                                      @Nonnegative int p1, @Nonnegative int p2mp1) {
    double rho1 = 1.0;
    double rho2 = rho1 / Layers.getRho1ToRho2(k[0]);
    double rho3 = rho2 / Layers.getRho1ToRho2(k[1]);
    return (TetrapolarResistance.of(s).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1 + 1, p2mp1).resistivity()
        - TetrapolarResistance.of(s).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1).resistivity())
        / (hStep / s.lCC());
  }
}
