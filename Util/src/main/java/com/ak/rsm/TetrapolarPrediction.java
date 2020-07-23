package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.util.Strings;

final class TetrapolarPrediction implements Prediction {
  @Nonnull
  private final Measurement measurement;
  @Nonnegative
  private final double resistivityPredicted;

  TetrapolarPrediction(@Nonnull Measurement measurement, @Nonnegative double resistivityPredicted) {
    this.measurement = measurement;
    this.resistivityPredicted = resistivityPredicted;
  }

  @Override
  public double[] getInequalityL2() {
    return new double[] {
        Inequality.absolute().applyAsDouble(measurement.getResistivity(), resistivityPredicted)
    };
  }

  @Override
  public String toString() {
    return String.format("%s; pred \u03c1 = %.3f %s", measurement, resistivityPredicted, Strings.OHM_METRE);
  }
}
