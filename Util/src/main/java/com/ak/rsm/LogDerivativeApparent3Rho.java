package com.ak.rsm;

import javax.annotation.Nonnull;

final class LogDerivativeApparent3Rho extends AbstractApparent3Rho {
  LogDerivativeApparent3Rho(@Nonnull TetrapolarSystem system) {
    super(new LogDerivativeApparent(system));
  }
}
