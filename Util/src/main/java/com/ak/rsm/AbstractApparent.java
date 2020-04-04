package com.ak.rsm;

import javax.annotation.Nonnull;

abstract class AbstractApparent extends AbstractResistanceSumValue {
  AbstractApparent(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  final double electrodesFactor() {
    return 2.0 / (1.0 / Math.abs(radius(-1.0)) - 1.0 / radius(1.0));
  }
}
