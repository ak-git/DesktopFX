package com.ak.rsm;

import javax.annotation.Nonnull;

abstract class AbstractNormalizedApparent extends AbstractApparent {
  AbstractNormalizedApparent(@Nonnull RelativeTetrapolarSystem system) {
    super(system);
  }

  @Override
  public final int sumFactor(int n) {
    return 1;
  }
}
