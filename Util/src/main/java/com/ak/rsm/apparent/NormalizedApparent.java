package com.ak.rsm.apparent;

import com.ak.rsm.system.RelativeTetrapolarSystem;

final class NormalizedApparent extends AbstractNormalizedApparent {
  NormalizedApparent(RelativeTetrapolarSystem system) {
    super(system);
  }

  @Override
  double multiply(double sums) {
    return 1.0 + electrodesFactor() * sums;
  }
}
