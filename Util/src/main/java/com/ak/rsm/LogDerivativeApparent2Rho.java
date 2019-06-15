package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

final class LogDerivativeApparent2Rho extends AbstractLogApparent2Rho {
  LogDerivativeApparent2Rho(@Nonnull TetrapolarSystem electrodeSystem) {
    super(electrodeSystem);
  }

  @Override
  public double value(double Lh, double sL, double sums) {
    return log(sums);
  }

  @Override
  DoubleBinaryOperator sum(double k, int sign) {
    return (Lh, sL) -> Layers.sum(n -> pow(k, n) * pow(n, 2.0), n -> pow(hypot(Lh * (1.0 + sign * sL), 4.0 * n), 3.0));
  }
}
