package com.ak.rsm;

import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnull;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log1p;
import static java.lang.StrictMath.pow;

final class Log1pApparent2Rho extends AbstractLogApparent2Rho {
  Log1pApparent2Rho(@Nonnull TetrapolarSystem electrodeSystem) {
    super(electrodeSystem);
  }

  @Override
  double value(double Lh, double sL, double sums) {
    double v = Lh * (1.0 - pow(sL, 2.0));
    if (v > 0) {
      v /= sL;
    }
    else {
      v = abs(v);
    }
    return log1p(v * sums);
  }

  @Override
  IntToDoubleFunction sum(double k, double Lh, double sL) {
    return sign -> Layers.sum(n -> pow(k, n), n -> hypot(Lh * (1.0 + sign * sL), 4.0 * n));
  }
}