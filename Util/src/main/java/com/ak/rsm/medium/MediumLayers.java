package com.ak.rsm.medium;

import com.ak.math.ValuePair;

import javax.annotation.Nonnull;

public sealed interface MediumLayers permits AbstractMediumLayers {
  @Nonnull
  ValuePair rho();

  @Nonnull
  double[] getRMS();
}
