package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.UnivariateFunction;
import tec.uom.se.unit.Units;

import static java.lang.StrictMath.PI;
import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

/**
 * Calculates <b>first derivative</b> resistance R<sub>m-n</sub> (in Ohm) by <b>L</b> normalized by rho1 for 2-layer model.
 * <br/>
 * <b>dR<sub>L</sub> / rho1</b>
 */
final class DerivativeRBySLNormalizedByRho1 implements UnivariateFunction {
  enum DerivateBy {
    S(1), L(-1);

    private final int factor;

    DerivateBy(int factor) {
      this.factor = factor;
    }
  }

  @Nonnull
  private final TetrapolarSystem electrodes;
  private final double k12;
  @Nonnull
  private final DerivateBy derivateBy;

  DerivativeRBySLNormalizedByRho1(double k12, @Nonnegative double sMetre, @Nonnegative double lMetre, @Nonnull DerivateBy derivateBy) {
    electrodes = new TetrapolarSystem(sMetre, lMetre, Units.METRE);
    this.k12 = k12;
    this.derivateBy = derivateBy;
  }

  @Override
  public double value(double hToL) {
    double a = derivateBy.factor / pow(electrodes.radiusMinus(), 2) + 1.0 / pow(electrodes.radiusPlus(), 2);
    double b = 2.0 * sumNkN(hToL);
    return (2.0 / PI) * (a + b);
  }

  private double sumNkN(@Nonnegative double hSI) {
    return ResistanceTwoLayer.sum(hSI, (n, b) -> pow(-1, n) * pow(-k12, n) *
        (derivateBy.factor * electrodes.radiusMinus() / pow(hypot(electrodes.radiusMinus(), b), 3.0) +
            electrodes.radiusPlus() / pow(hypot(electrodes.radiusPlus(), b), 3.0)));
  }
}
