package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class DerivativeApparentByK2Rho extends AbstractApparent2Rho {
  DerivativeApparentByK2Rho(@Nonnull RelativeTetrapolarSystem system) {
    super(new DerivativeApparentByK(system));
  }

  @Override
  double kFactor(double k, @Nonnegative int n) {
    return StrictMath.pow(k, n - 1.0) * sumFactor(n);
  }
}
