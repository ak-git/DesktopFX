package com.ak.rsm.medium;

import javax.annotation.Nonnull;

import com.ak.math.ValuePair;

public interface MediumLayers {
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
