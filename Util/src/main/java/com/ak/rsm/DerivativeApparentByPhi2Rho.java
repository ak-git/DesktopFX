package com.ak.rsm;

import javax.annotation.Nonnull;

final class DerivativeApparentByPhi2Rho extends AbstractApparent2Rho {
  DerivativeApparentByPhi2Rho(@Nonnull RelativeTetrapolarSystem system) {
    super(new DerivativeApparentByPhi(system));
  }
}
