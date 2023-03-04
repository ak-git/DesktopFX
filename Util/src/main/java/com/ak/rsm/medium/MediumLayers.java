package com.ak.rsm.medium;

import com.ak.math.ValuePair;

import javax.annotation.Nonnull;

public sealed interface MediumLayers permits AbstractMediumLayers {
  @Nonnull
  ValuePair rho();

  @Nonnull
  ValuePair h1();

  @Nonnull
  default ValuePair rho1() {
    return rho();
  }

  @Nonnull
  default ValuePair rho2() {
    return rho();
  }

  @Nonnull
  double[] getRMS();
}
