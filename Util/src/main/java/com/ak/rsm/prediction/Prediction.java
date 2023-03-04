package com.ak.rsm.prediction;

import javax.annotation.Nonnull;

public sealed interface Prediction permits AbstractPrediction {
  double getPredicted();

  @Nonnull
  double[] getInequalityL2();
}
