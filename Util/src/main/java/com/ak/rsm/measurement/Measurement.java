package com.ak.rsm.measurement;

import com.ak.rsm.prediction.Prediction;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public sealed interface Measurement extends Resistance permits DerivativeMeasurement, TetrapolarMeasurement {
  @Nonnull
  InexactTetrapolarSystem inexact();

  @Nonnull
  @Override
  default TetrapolarSystem system() {
    return inexact().system();
  }

  @Nonnull
  Measurement merge(@Nonnull Measurement that);

  @Nonnull
  Prediction toPrediction(@Nonnull RelativeMediumLayers kw, @Nonnegative double rho1);
}
