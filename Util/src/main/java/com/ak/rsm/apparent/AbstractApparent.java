package com.ak.rsm.apparent;

import com.ak.rsm.system.RelativeTetrapolarSystem;

import java.util.Objects;

abstract class AbstractApparent {
  private final RelativeTetrapolarSystem system;

  AbstractApparent(RelativeTetrapolarSystem system) {
    this.system = Objects.requireNonNull(system);
  }

  final double factor(double sign) {
    return system.factor(sign);
  }

  final double electrodesFactor() {
    return 2.0 / (1.0 / factor(-1) - 1.0 / factor(1));
  }
}
