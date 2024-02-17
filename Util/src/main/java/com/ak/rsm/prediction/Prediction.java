package com.ak.rsm.prediction;

public sealed interface Prediction permits AbstractPrediction {
  double getPredicted();

  double[] getInequalityL2();
}
