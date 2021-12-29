package com.ak.rsm.measurement;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.medium.RelativeMediumLayers;
import com.ak.rsm.prediction.Prediction;
import com.ak.rsm.system.InexactTetrapolarSystem;

public interface Measurement {
  @Nonnull
  InexactTetrapolarSystem system();

  @Nonnegative
  double resistivity();

  @Nonnull
  Measurement merge(@Nonnull Measurement that);

  @Nonnull
  @ParametersAreNonnullByDefault
  Prediction toPrediction(RelativeMediumLayers kw, @Nonnegative double rho1);
}
