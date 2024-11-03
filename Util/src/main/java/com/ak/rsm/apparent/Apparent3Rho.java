package com.ak.rsm.apparent;

import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;

public class Apparent3Rho extends AbstractApparentRho {
  private Apparent3Rho(ResistanceSumValue apparent) {
    super(apparent);
  }

  public double value(double k12, double k23, @Nonnegative double hToL, @Nonnegative int p1, @Nonnegative int p2mp1) {
    double[] qn = Layers.qn(k12, k23, p1, p2mp1);
    return value(hToL, n -> qn[n] * sumFactor(n));
  }

  public static Apparent3Rho newApparentDivRho1(RelativeTetrapolarSystem system) {
    return new Apparent3Rho(new NormalizedApparent(system));
  }

  public static double newDerApparentByH1PhiDivRho1(TetrapolarSystem system, double[] k, @Nonnegative double hStep,
                                                    @Nonnegative int p1, @Nonnegative int p2mp1, double dh1) {
    double rho1 = 1.0;
    double rho2 = rho1 / Layers.getRho1ToRho2(k[0]);
    double rho3 = rho2 / Layers.getRho1ToRho2(k[1]);
    return TetrapolarDerivativeResistance.of(system).dh(dh1).rho1(rho1).rho2(rho2).rho3(rho3)
        .hStep(hStep).p(p1, p2mp1).derivativeResistivity();
  }
}
