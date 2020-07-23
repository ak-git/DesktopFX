package com.ak.rsm;

interface DerivativeMeasurement extends Measurement {
  double getDerivativeResistivity();

  double getDerivativeLogResistivity();
}
