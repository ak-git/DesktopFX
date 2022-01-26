package com.ak.rsm.apparent;

import javax.annotation.Nonnull;

import com.ak.rsm.system.RelativeTetrapolarSystem;

import static java.lang.StrictMath.log1p;

final class Log1pApparent extends AbstractNormalizedApparent {
  Log1pApparent(@Nonnull RelativeTetrapolarSystem system) {
    super(system);
  }

  @Override
  double multiply(double sums) {
    return log1p(electrodesFactor() * sums);
  }
}
