package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.util.Strings;

final class TetrapolarDerivativePrediction implements Prediction {
  @Nonnull
  private final Prediction prediction;
  private final double diffResistivityPredicted;
  @Nonnegative
  private final double l2Diff;

  TetrapolarDerivativePrediction(@Nonnull DerivativeMeasurement measurement, @Nonnegative double resistivityPredicted, double diffResistivityPredicted) {
    prediction = new TetrapolarPrediction(measurement, resistivityPredicted);
    this.diffResistivityPredicted = diffResistivityPredicted;
    l2Diff = Inequality.proportional().applyAsDouble(measurement.getDerivativeResistivity(), diffResistivityPredicted);
  }

  @Override
  public double getInequalityL2() {
    return l2Diff;
  }

  @Override
  public String toString() {
    return String.format("%s, %s", prediction, Strings.dRhoByH(diffResistivityPredicted));
  }
}
