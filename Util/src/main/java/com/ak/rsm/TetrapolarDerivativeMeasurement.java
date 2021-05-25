package com.ak.rsm;

import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Strings;

final class TetrapolarDerivativeMeasurement implements DerivativeMeasurement {
  @Nonnull
  private final Measurement measurement;
  @Nonnegative
  private final double dRhoBydPhi;

  TetrapolarDerivativeMeasurement(@Nonnull Measurement measurementBefore,
                                  @Nonnull Measurement measurementAfter, double dh) {
    measurement = measurementBefore;
    dRhoBydPhi = (measurementAfter.getResistivity() - measurement.getResistivity()) / (dh / getSystem().toExact().getL());
  }

  @Override
  public double getResistivity() {
    return measurement.getResistivity();
  }

  @Override
  public double getDerivativeResistivity() {
    return dRhoBydPhi;
  }

  @Override
  @Nonnull
  public Prediction toPrediction(@Nonnull RelativeMediumLayers<Double> kw, @Nonnegative double rho1) {
    return new TetrapolarDerivativePrediction(getSystem(), kw, rho1, new double[] {getResistivity(), getDerivativeResistivity()});
  }

  @Override
  public InexactTetrapolarSystem getSystem() {
    return measurement.getSystem();
  }

  @Override
  public String toString() {
    return "%s, %s".formatted(String.valueOf(measurement), Strings.dRhoByPhi(dRhoBydPhi));
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static List<DerivativeMeasurement> of(InexactTetrapolarSystem[] systems, double[] rOhmsBefore, double[] rOhmsAfter, double dh) {
    return IntStream.range(0, systems.length)
        .mapToObj(i -> new TetrapolarDerivativeMeasurement(
            new TetrapolarMeasurement(systems[i], rOhmsBefore[i]),
            new TetrapolarMeasurement(systems[i], rOhmsAfter[i]),
            dh
        ))
        .map(DerivativeMeasurement.class::cast)
        .toList();
  }
}
