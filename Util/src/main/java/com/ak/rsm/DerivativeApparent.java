package com.ak.rsm;

import javax.annotation.Nonnull;

final class DerivativeApparent extends AbstractDerivativeApparent {
  DerivativeApparent(@Nonnull RelativeTetrapolarSystem system) {
    super(system);
  }

  @Override
  double multiply(double sums) {
    return -16.0 * electrodesFactor() * sums;
  }
}
