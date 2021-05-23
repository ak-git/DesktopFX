package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Strings;

final class TetrapolarDerivativePrediction implements Prediction {
  @Nonnull
  private final Prediction prediction;
  private final double diffResistivityPredicted;

  @ParametersAreNonnullByDefault
  TetrapolarDerivativePrediction(InexactTetrapolarSystem system, RelativeMediumLayers<Double> layers, @Nonnegative double rho1) {
    prediction = new TetrapolarPrediction(system, layers, rho1);
    diffResistivityPredicted = new DerivativeApparent2Rho(system.toExact().toRelative()).value(layers.k12(), layers.hToL()) * rho1;
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
