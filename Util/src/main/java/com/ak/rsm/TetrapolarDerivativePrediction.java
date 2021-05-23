package com.ak.rsm;

import java.util.Arrays;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.util.Strings;

final class TetrapolarDerivativePrediction implements Prediction {
  @Nonnull
  private final Prediction prediction;
  private final double diffResistivityPredicted;
  @Nonnegative
  private final double[] l2Diff;

  @ParametersAreNonnullByDefault
  TetrapolarDerivativePrediction(DerivativeMeasurement measurement, RelativeMediumLayers<Double> layers, @Nonnegative double rho1) {
    prediction = new TetrapolarPrediction(measurement, layers, rho1);
    diffResistivityPredicted = new DerivativeApparent2Rho(measurement.getSystem().toExact().toRelative()).value(layers.k12(), layers.hToL()) * rho1;
    l2Diff = DoubleStream.concat(
        Arrays.stream(prediction.getInequalityL2()),
        DoubleStream.of(Inequality.proportional().applyAsDouble(measurement.getDerivativeResistivity(), diffResistivityPredicted))
    ).toArray();
  }

  @Override
  public double[] getInequalityL2() {
    return Arrays.copyOf(l2Diff, l2Diff.length);
  }

  @Override
  public double getResistivityPredicted() {
    return diffResistivityPredicted;
  }

  @Override
  public double[] getHorizons() {
    return prediction.getHorizons();
  }

  @Override
  public String toString() {
    return "%s, %s".formatted(String.valueOf(prediction), Strings.dRhoByPhi(diffResistivityPredicted));
  }
}
