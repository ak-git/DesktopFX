package com.ak.rsm.prediction;

import com.ak.util.Strings;

import javax.annotation.Nonnegative;

final class TetrapolarPrediction extends AbstractPrediction {
  TetrapolarPrediction(@Nonnegative double resistivityPredicted, @Nonnegative double inequalityL2) {
    super(resistivityPredicted, new double[] {inequalityL2});
  }

  @Override
  public String toString() {
    return "predicted %s".formatted(Strings.rho(getPredicted()));
  }
}
