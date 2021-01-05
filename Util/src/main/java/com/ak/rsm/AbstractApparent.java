package com.ak.rsm;

import javax.annotation.Nonnull;

abstract class AbstractApparent extends AbstractResistanceSumValue {
  AbstractApparent(@Nonnull RelativeTetrapolarSystem system) {
    super(system);
  }

  final double electrodesFactor() {
    return 2.0 / (1.0 / factor(-1) - 1.0 / factor(1));
  }
}
