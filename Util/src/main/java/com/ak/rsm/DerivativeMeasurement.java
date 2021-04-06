package com.ak.rsm;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

interface DerivativeMeasurement extends Measurement {
  double getDerivativeResistivity();

  default double getDerivativeLogResistivity() {
    return StrictMath.log(Math.abs(getDerivativeResistivity()));
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static DerivativeMeasurement newInstance(DerivativeMeasurement m, InexactTetrapolarSystem s) {
    return new DerivativeMeasurement() {
      @Override
      public double getDerivativeResistivity() {
        return m.getDerivativeResistivity();
      }

      @Nonnull
      @Override
      public InexactTetrapolarSystem getSystem() {
        return s;
      }

      @Override
      public double getResistivity() {
        return m.getResistivity();
      }
    };
  }
}
