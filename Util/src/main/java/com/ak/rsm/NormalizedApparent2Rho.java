package com.ak.rsm;

import javax.annotation.Nonnull;

/**
 * Calculates Apparent Resistance divided by Rho1
 */
final class NormalizedApparent2Rho extends AbstractApparent2Rho {
  NormalizedApparent2Rho(@Nonnull RelativeTetrapolarSystem system) {
    super(new NormalizedApparent(system));
  }
}
