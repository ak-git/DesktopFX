package com.ak.rsm.apparent;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.system.RelativeTetrapolarSystem;

/**
 * dRho(apparent) by k
 */
final class DerivativeApparentByK extends AbstractResistanceSumValue {
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
