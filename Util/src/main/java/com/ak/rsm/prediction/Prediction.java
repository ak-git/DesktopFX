package com.ak.rsm.prediction;

import javax.annotation.Nonnull;

public interface Prediction {
  double getPredicted();

  @Nonnull
  double[] getHorizons();

  @Nonnull
  double[] getInequalityL2();
}
