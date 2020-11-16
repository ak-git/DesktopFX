package com.ak.rsm;

import javax.annotation.Nonnull;

final class Log1pApparent3Rho extends AbstractApparent3Rho {
  Log1pApparent3Rho(@Nonnull TetrapolarSystem system) {
    super(new Log1pApparent(system));
  }
}
