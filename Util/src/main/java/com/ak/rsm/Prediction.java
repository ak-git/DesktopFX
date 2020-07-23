package com.ak.rsm;

import javax.annotation.Nonnull;

interface Prediction {
  @Nonnull
  double[] getInequalityL2();
}
