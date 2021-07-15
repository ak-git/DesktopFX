package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class SecondDerivativeApparentByPhiK2Rho extends Apparent2Rho {
  SecondDerivativeApparentByPhiK2Rho(@Nonnull RelativeTetrapolarSystem system) {
    super(new SecondDerivativeApparentByPhiK(system));
  }

  @Override
  double kFactor(double k, @Nonnegative int n) {
    return StrictMath.pow(k, n - 1.0) * sumFactor(n);
  }
}
