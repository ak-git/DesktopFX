package com.ak.rsm.apparent;

import com.ak.rsm.system.RelativeTetrapolarSystem;

import javax.annotation.Nonnull;

import static java.lang.StrictMath.log1p;

final class Log1pNormalizedApparent extends AbstractNormalizedApparent {
  Log1pNormalizedApparent(@Nonnull RelativeTetrapolarSystem system) {
    super(system);
  }

  @Override
  double multiply(double sums) {
    return log1p(electrodesFactor() * sums);
  }
}
