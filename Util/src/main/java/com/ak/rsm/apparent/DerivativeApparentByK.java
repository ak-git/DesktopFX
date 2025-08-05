package com.ak.rsm.apparent;

import com.ak.rsm.system.RelativeTetrapolarSystem;

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
  public int sumFactor(int n) {
    return n;
  }
}
