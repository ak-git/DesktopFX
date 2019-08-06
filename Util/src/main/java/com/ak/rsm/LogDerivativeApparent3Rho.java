package com.ak.rsm;

import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnegative;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

final class LogDerivativeApparent3Rho extends AbstractLogApparent3Rho {
  LogDerivativeApparent3Rho(@Nonnegative double sToL, @Nonnegative double Lh) {
    super(sToL, Lh);
  }

  @Override
  double innerValue(double sums) {
    return log(Math.abs(sums));
  }

  @Override
  IntToDoubleFunction sum(double[] q) {
    return sign -> Layers.sum(n -> q[n] * pow(n, 2.0), n -> pow(hypot(Lh() * (1.0 + sign * sToL()), 4.0 * n), 3.0));
  }
}
