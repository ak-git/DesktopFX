package com.ak.rsm;

import javax.annotation.Nonnull;

final class DerivativeApparentByPhi3Rho extends AbstractApparent3Rho {
  DerivativeApparentByPhi3Rho(@Nonnull RelativeTetrapolarSystem system) {
    super(new DerivativeApparentByPhi(system));
  }
}
