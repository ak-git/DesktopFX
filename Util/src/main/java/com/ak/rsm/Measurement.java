package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

interface Measurement {
  @Nonnull
  TetrapolarSystem system();

  @Nonnegative
  double resistivity();

  @Nonnull
  Prediction toPrediction(@Nonnull RelativeMediumLayers kw, @Nonnegative double rho1);

  @Nonnull
  default Measurement merge(@Nonnull Measurement that) {
    throw new UnsupportedOperationException(that.toString());
  }
}
