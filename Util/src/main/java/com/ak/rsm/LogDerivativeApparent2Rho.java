package com.ak.rsm;

import javax.annotation.Nonnull;

final class LogDerivativeApparent2Rho extends AbstractApparent2Rho {
  LogDerivativeApparent2Rho(@Nonnull TetrapolarSystem system) {
    super(new LogDerivativeApparent(system));
  }
}
