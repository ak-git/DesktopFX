package com.ak.rsm.apparent;

import javax.annotation.Nonnull;

import com.ak.rsm.system.RelativeTetrapolarSystem;

abstract class AbstractApparent {
  @Nonnull
  private final RelativeTetrapolarSystem system;

  AbstractApparent(@Nonnull RelativeTetrapolarSystem system) {
    this.system = system;
  }

  final double factor(double sign) {
    return system.factor(sign);
  }

  final double electrodesFactor() {
    return 2.0 / (1.0 / factor(-1) - 1.0 / factor(1));
  }
}
