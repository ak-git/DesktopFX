package com.ak.rsm;

import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

final class LogDerivativeApparent2Rho extends AbstractLogApparent2Rho {
  LogDerivativeApparent2Rho(@Nonnull TetrapolarSystem electrodeSystem) {
    super(electrodeSystem);
  }

  @Override
  double value(double Lh, double sL, double sums) {
    return log(Math.abs(sums));
  }

  @Override
  IntToDoubleFunction sum(double k, double Lh, double sL) {
    return sign -> Layers.sum(n -> pow(k, n) * pow(n, 2.0), n -> pow(hypot(Lh * (1.0 + sign * sL), 4.0 * n), 3.0));
  }
}
