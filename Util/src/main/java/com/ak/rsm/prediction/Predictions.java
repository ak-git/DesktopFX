package com.ak.rsm.prediction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import com.ak.rsm.medium.RelativeMediumLayers;

public enum Predictions {
  ;

  @Nonnull
  @ParametersAreNonnullByDefault
  public static Prediction toPrediction(Measurement measurement, RelativeMediumLayers kw, @Nonnegative double rho1) {
    if (measurement instanceof TetrapolarMeasurement tm) {
      return TetrapolarPrediction.of(tm.system(), kw, rho1, measurement.resistivity());
    }
    else {
      throw new IllegalArgumentException(measurement.toString());
    }
  }
}
