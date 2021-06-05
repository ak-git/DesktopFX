package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * dRho(apparent) by k
 */
final class DerivativeApparentByK extends AbstractApparent {
  DerivativeApparentByK(@Nonnull RelativeTetrapolarSystem system) {
    super(system);
  }

  @Override
  double multiply(double sums) {
    return electrodesFactor() * sums;
  }

  @Override
  public int sumFactor(@Nonnegative int n) {
    return n;
  }
}
