package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

interface DerivativeMeasurement extends Measurement {
  double getDerivativeResistivity();

  @Override
  @Nonnull
  default Prediction toPrediction(@Nonnull RelativeMediumLayers<Double> kw, @Nonnegative double rho1) {
    return new TetrapolarDerivativePrediction(getSystem(), kw, rho1);
  }

  default double getDerivativeLogResistivity() {
    return StrictMath.log(Math.abs(getDerivativeResistivity()));
  }
}
