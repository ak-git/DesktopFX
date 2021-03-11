package com.ak.rsm;

import javax.annotation.Nonnull;

final class DerivativeRbyH extends AbstractDerivativeApparent {
  DerivativeRbyH(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  @Override
  double multiply(double sums) {
    return -64.0 * sums / StrictMath.pow(getL(), 2.0);
  }
}
