package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

interface Measurement {
  @Nonnull
  TetrapolarSystem getSystem();

  @Nonnull
  Measurement newInstance(@Nonnull TetrapolarSystem system);

  @Nonnegative
  double getResistivity();

  @Nonnull
  Prediction toPrediction(@Nonnull RelativeMediumLayers<Double> kw, @Nonnegative double rho1);

  @Nonnull
  default Measurement merge(@Nonnull Measurement that) {
    throw new UnsupportedOperationException(that.toString());
  }
}
