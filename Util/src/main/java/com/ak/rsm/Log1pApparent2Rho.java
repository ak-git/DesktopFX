package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log1p;
import static java.lang.StrictMath.pow;

final class Log1pApparent2Rho extends AbstractLogApparent2Rho {
  Log1pApparent2Rho(@Nonnull TetrapolarSystem electrodeSystem) {
    super(electrodeSystem);
  }

  @Override
  public double value(double Lh, double sL, double sums) {
    return log1p((Lh * (1.0 - pow(sL, 2.0)) / sL) * sums);
  }

  @Override
  DoubleBinaryOperator sum(double k, int sign) {
    return (Lh, sL) -> Layers.sum(n -> pow(k, n), n -> hypot(Lh * (1.0 + sign * sL), 4.0 * n));
  }
}
