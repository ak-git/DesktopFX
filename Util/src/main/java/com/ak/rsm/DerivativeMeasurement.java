package com.ak.rsm;

interface DerivativeMeasurement extends Measurement {
  double getDerivativeResistivity();

  default double getDerivativeLogResistivity() {
    return StrictMath.log(Math.abs(getDerivativeResistivity()));
  }
}
