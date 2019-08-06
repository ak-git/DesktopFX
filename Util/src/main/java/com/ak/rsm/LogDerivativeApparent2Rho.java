package com.ak.rsm;

import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnegative;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

final class LogDerivativeApparent2Rho extends AbstractLogApparent2Rho {
  LogDerivativeApparent2Rho(@Nonnegative double sToL) {
    super(sToL);
  }

  @Override
  double innerValue(double Lh, double sums) {
    return log(Math.abs(sums));
  }

  @Override
  IntToDoubleFunction sum(double k, double Lh) {
    return sign -> Layers.sum(n -> pow(k, n) * pow(n, 2.0), n -> pow(hypot(Lh * (1.0 + sign * sToL()), 4.0 * n), 3.0));
  }
}
