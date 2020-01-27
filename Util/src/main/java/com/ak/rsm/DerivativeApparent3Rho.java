package com.ak.rsm;

import javax.annotation.Nonnull;

final class DerivativeApparent3Rho extends AbstractApparent3Rho {
  DerivativeApparent3Rho(@Nonnull TetrapolarSystem system) {
    super(new DerivativeApparent(system));
  }
}
