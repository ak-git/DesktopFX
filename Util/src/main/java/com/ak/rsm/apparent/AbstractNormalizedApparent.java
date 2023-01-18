package com.ak.rsm.apparent;

import javax.annotation.Nonnull;

import com.ak.rsm.system.RelativeTetrapolarSystem;

abstract class AbstractNormalizedApparent extends AbstractResistanceSumValue {
  AbstractNormalizedApparent(@Nonnull RelativeTetrapolarSystem system) {
    super(system);
  }

  @Override
  public final int sumFactor(int n) {
    return 1;
  }
}
