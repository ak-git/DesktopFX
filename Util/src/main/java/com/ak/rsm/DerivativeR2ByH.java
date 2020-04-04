package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class DerivativeR2ByH extends AbstractApparent2Rho {
  DerivativeR2ByH(@Nonnull TetrapolarSystem system) {
    super(new DerivativeRbyH(system));
  }

  double value(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double h) {
    return value(Layers.getK12(rho1, rho2), h) * rho1 / Math.PI;
  }
}
