package com.ak.rsm;

import java.util.Arrays;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import tec.uom.se.unit.Units;

final class TetrapolarDerivativePrediction implements Prediction {
  @Nonnull
  private final Prediction prediction;
  private final double diffResistivityPredicted;
  @Nonnegative
  private final double l2Diff;

  TetrapolarDerivativePrediction(@Nonnull DerivativeMeasurement measurement, @Nonnegative double resistivityPredicted, double diffResistivityPredicted) {
    prediction = new TetrapolarPrediction(measurement, resistivityPredicted);
    this.diffResistivityPredicted = diffResistivityPredicted;
    l2Diff = Inequality.absolute().applyAsDouble(measurement.getDerivativeResistivity(), diffResistivityPredicted);
  }

  @Override
  public double[] getInequalityL2() {
    return DoubleStream.concat(Arrays.stream(prediction.getInequalityL2()), DoubleStream.of(l2Diff)).toArray();
  }

  @Override
  public String toString() {
    return String.format("%s, d\u03c1/dh = %.0f %s", prediction, diffResistivityPredicted, Units.OHM);
  }
}
