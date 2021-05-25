package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class DerivativeRbyH extends AbstractDerivativeApparent {
  @Nonnegative
  private final double lCC;

  DerivativeRbyH(@Nonnull TetrapolarSystem system) {
    super(system.toRelative());
    lCC = system.getL();
  }

  double getL() {
    return lCC;
  }

  @Override
  double multiply(double sums) {
    return -64.0 * sums / StrictMath.pow(getL(), 2.0);
  }
}
