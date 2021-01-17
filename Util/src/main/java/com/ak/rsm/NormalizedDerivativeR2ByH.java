package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class NormalizedDerivativeR2ByH extends AbstractApparent2Rho {
  NormalizedDerivativeR2ByH(@Nonnull TetrapolarSystem system) {
    super(new DerivativeRbyH(system));
  }

  @Override
  double value(double k, @Nonnegative double hToL) {
    return super.value(k, hToL) / Math.PI;
  }
}
