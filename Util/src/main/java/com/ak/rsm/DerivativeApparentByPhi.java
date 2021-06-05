package com.ak.rsm;

import javax.annotation.Nonnull;

/**
 * dRho(apparent) by dPhi. phi = h / L
 */
final class DerivativeApparentByPhi extends AbstractDerivativeApparent {
  DerivativeApparentByPhi(@Nonnull RelativeTetrapolarSystem system) {
    super(system);
  }

  @Override
  double multiply(double sums) {
    return -16.0 * electrodesFactor() * sums;
  }
}
