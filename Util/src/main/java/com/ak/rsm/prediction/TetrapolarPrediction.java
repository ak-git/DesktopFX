package com.ak.rsm.prediction;

import com.ak.util.Strings;

final class TetrapolarPrediction extends AbstractPrediction {
  TetrapolarPrediction(double resistivityPredicted, double inequalityL2) {
    super(resistivityPredicted, new double[] {inequalityL2});
  }

  @Override
  public String toString() {
    return "predicted %s".formatted(Strings.rho(getPredicted()));
  }
}
