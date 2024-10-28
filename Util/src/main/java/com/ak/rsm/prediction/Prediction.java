package com.ak.rsm.prediction;

public interface Prediction {
  double getPredicted();

  double[] getInequalityL2();
}
