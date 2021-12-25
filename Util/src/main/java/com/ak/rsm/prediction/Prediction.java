package com.ak.rsm.prediction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface Prediction {
  @Nonnegative
  double getResistivityPredicted();

  @Nonnull
  double[] getHorizons();

  @Nonnull
  double[] getInequalityL2();
}
