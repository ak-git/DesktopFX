package com.ak.rsm;

import javax.annotation.Nonnull;

final class DerivativeApparent2Rho extends AbstractApparent2Rho {
  DerivativeApparent2Rho(@Nonnull TetrapolarSystem system) {
    super(new DerivativeApparent(system));
  }
}
