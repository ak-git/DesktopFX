package com.ak.rsm;

import java.util.Collection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

interface Measurement {
  @Nonnull
  InexactTetrapolarSystem getSystem();

  @Nonnegative
  double getResistivity();

  default double getLogResistivity() {
    return StrictMath.log(getResistivity());
  }

  @Nonnull
  default Prediction toPrediction(@Nonnull RelativeMediumLayers<Double> kw, @Nonnegative double rho1) {
    return new TetrapolarPrediction(getSystem(), kw, rho1);
  }

  @Nonnull
  default Measurement merge(@Nonnull Measurement that) {
    throw new UnsupportedOperationException(that.toString());
  }

  @Nonnegative
  static double getBaseL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.parallelStream().mapToDouble(m -> m.getSystem().toExact().getL()).max().orElseThrow();
  }
}
