package com.ak.rsm.apparent;

import com.ak.rsm.system.RelativeTetrapolarSystem;

import javax.annotation.Nonnegative;

/**
 * dRho(apparent) by k
 */
final class DerivativeApparentByK extends AbstractResistanceSumValue {
  DerivativeApparentByK(RelativeTetrapolarSystem system) {
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
