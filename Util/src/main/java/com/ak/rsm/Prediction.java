package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

interface Prediction {
  @Nonnegative
  double getInequalityL2();

  @Nonnegative
  double getResistivityPredicted();

  @Nonnull
  double[] getHorizons();
}
