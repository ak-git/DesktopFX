package com.ak.rsm;

import javax.annotation.Nonnegative;

interface MediumLayers extends RelativeMediumLayers {
  @Nonnegative
  double rho();

  @Nonnegative
  default double rho1() {
    return rho();
  }

  @Nonnegative
  default double rho2() {
    return rho();
  }

  @Override
  default double k12() {
    return Layers.getK12(rho1(), rho2());
  }
}
