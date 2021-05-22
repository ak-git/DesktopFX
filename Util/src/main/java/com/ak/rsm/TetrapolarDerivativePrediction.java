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

  TetrapolarDerivativePrediction(@Nonnull DerivativeMeasurement measurement,
                                 @Nonnegative Prediction prediction, double diffResistivityPredicted) {
    this.prediction = prediction;
    this.diffResistivityPredicted = diffResistivityPredicted;
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
    return "%s, %s".formatted(String.valueOf(prediction), Strings.dRhoByH(diffResistivityPredicted));
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static Prediction of(DerivativeMeasurement m, RelativeMediumLayers<Double> layers, @Nonnegative double rho1) {
    TetrapolarSystem system = m.getSystem().toExact();
    return new TetrapolarDerivativePrediction(m, new TetrapolarPrediction(m, layers, rho1),
        new DerivativeApparent2Rho(system.toRelative()).value(layers.k12(), layers.h() / system.getL()) * rho1
    );
  }
}
