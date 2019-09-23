package com.ak.rsm;

import javax.annotation.Nonnull;

abstract class AbstractApparent {
  @Nonnull
  private final TetrapolarSystem system;

  AbstractApparent(@Nonnull TetrapolarSystem system) {
    this.system = system;
  }

  final double electrodesFactor() {
    return 2.0 / (1.0 / Math.abs(system.radiusMns()) - 1.0 / system.radiusPls());
  }

  final double radius(double sign) {
    if (sign < 0) {
      return system.radiusMns();
    }
    else {
      return system.radiusPls();
    }
  }
}
