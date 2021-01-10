package com.ak.rsm;

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
  private final double l2Diff;

  TetrapolarDerivativePrediction(@Nonnull DerivativeMeasurement measurement,
                                 @Nonnegative Prediction prediction, double diffResistivityPredicted) {
    this.prediction = prediction;
    this.diffResistivityPredicted = diffResistivityPredicted;
    l2Diff = Inequality.proportional().applyAsDouble(measurement.getDerivativeResistivity(), diffResistivityPredicted);
  }

  @Override
  public double getInequalityL2() {
    return l2Diff;
  }

  @Override
  public double getResistivityPredicted() {
    return diffResistivityPredicted;
  }

  @Override
  public String toString() {
    return "%s, %s".formatted(String.valueOf(prediction), Strings.dRhoByH(diffResistivityPredicted));
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static Prediction of(DerivativeMeasurement m, RelativeMediumLayers layers, @Nonnegative double rho1) {
    TetrapolarSystem system = m.getSystem().toExact();
    return new TetrapolarDerivativePrediction(m, new TetrapolarPrediction(m, layers, rho1),
        new DerivativeApparent2Rho(system).value(layers.k12(), layers.h() / system.getL()) * rho1
    );
  }
}
