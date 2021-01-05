package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.util.Strings;

final class TetrapolarPrediction implements Prediction {
  @Nonnull
  private final Measurement measurement;
  @Nonnegative
  private final double resistivityPredicted;

  TetrapolarPrediction(@Nonnull Measurement measurement, @Nonnegative double resistivityPredicted) {
    this.measurement = measurement;
    this.resistivityPredicted = resistivityPredicted;
  }

  @Override
  public double getInequalityL2() {
    return Inequality.proportional().applyAsDouble(measurement.getResistivity(), resistivityPredicted);
  }

  @Override
  public double getResistivityPredicted() {
    return resistivityPredicted;
  }

  @Override
  public String toString() {
    return "%s; predicted %s".formatted(String.valueOf(measurement), Strings.rho(resistivityPredicted));
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static Prediction of(Measurement m, RelativeMediumLayers layers, @Nonnegative double rho1) {
    TetrapolarSystem system = m.getSystem();
    double resistivityPredicted = new NormalizedApparent2Rho(system).value(layers.k12(), layers.h() / system.getL()) * rho1;
    return new TetrapolarPrediction(m, resistivityPredicted);
  }
}
