package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

interface MediumLayers<D> {
  @Nonnull
  D rho();

  @Nonnull
  D h1();

  @Nonnull
  default D rho1() {
    return rho();
  }

  @Nonnull
  default D rho2() {
    return rho();
  }

  @Nonnegative
  double[] getInequalityL2();
}
