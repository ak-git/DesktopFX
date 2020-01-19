package com.ak.rsm;

import javax.annotation.Nonnull;

final class Log1pApparent2Rho extends AbstractApparent2Rho {
  Log1pApparent2Rho(@Nonnull TetrapolarSystem system) {
    super(new Log1pApparent(system));
  }
}
