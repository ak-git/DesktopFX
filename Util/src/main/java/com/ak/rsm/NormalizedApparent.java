package com.ak.rsm;

import javax.annotation.Nonnull;

final class NormalizedApparent extends AbstractNormalizedApparent {
  NormalizedApparent(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  @Override
  double multiply(double sums) {
    return 1.0 + electrodesFactor() * sums;
  }
}
