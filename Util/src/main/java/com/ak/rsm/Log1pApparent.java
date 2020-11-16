package com.ak.rsm;

import javax.annotation.Nonnull;

import static java.lang.StrictMath.log1p;

final class Log1pApparent extends AbstractNormalizedApparent {
  Log1pApparent(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  @Override
  double multiply(double sums) {
    return log1p(electrodesFactor() * sums);
  }
}
