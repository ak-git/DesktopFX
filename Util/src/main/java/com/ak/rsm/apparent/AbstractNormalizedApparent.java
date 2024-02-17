package com.ak.rsm.apparent;

import com.ak.rsm.system.RelativeTetrapolarSystem;

abstract class AbstractNormalizedApparent extends AbstractResistanceSumValue {
  AbstractNormalizedApparent(RelativeTetrapolarSystem system) {
    super(system);
  }

  @Override
  public final int sumFactor(int n) {
    return 1;
  }
}
