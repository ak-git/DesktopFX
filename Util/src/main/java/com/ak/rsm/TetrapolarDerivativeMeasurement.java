package com.ak.rsm;

import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Strings;

final class TetrapolarDerivativeMeasurement implements DerivativeMeasurement {
  @Nonnull
  private final Measurement measurement;
  private final double dRhoBydPhi;

  TetrapolarDerivativeMeasurement(@Nonnull Measurement measurementBefore,
                                  @Nonnull Measurement measurementAfter, double dh) {
    measurement = measurementBefore;
    dRhoBydPhi = (measurementAfter.getResistivity() - measurement.getResistivity()) / (dh / getSystem().getL());
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
  public Prediction toPrediction(@Nonnull RelativeMediumLayers kw, @Nonnegative double rho1) {
    return new TetrapolarDerivativePrediction(getSystem(), kw, rho1, new double[] {getResistivity(), getDerivativeResistivity()});
  }

  @Override
  public TetrapolarSystem getSystem() {
    return measurement.getSystem();
  }

  @Override
  public String toString() {
    return "%s, %s".formatted(measurement, Strings.dRhoByPhi(dRhoBydPhi));
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static List<DerivativeMeasurement> of(TetrapolarSystem[] systems, double[] rOhmsBefore, double[] rOhmsAfter, double dh) {
    return IntStream.range(0, systems.length)
        .mapToObj(i -> new TetrapolarDerivativeMeasurement(
            new TetrapolarMeasurement(systems[i], rOhmsBefore[i]),
            new TetrapolarMeasurement(systems[i], rOhmsAfter[i]),
            dh
        ))
        .map(DerivativeMeasurement.class::cast)
        .toList();
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static List<Measurement> of(TetrapolarSystem[] systems,
                              ToDoubleFunction<TetrapolarSystem> toOhmsBefore,
                              ToDoubleFunction<TetrapolarSystem> toOhmsAfter,
                              double dh) {
    return Arrays.stream(systems)
        .map(s -> new TetrapolarDerivativeMeasurement(
            new TetrapolarMeasurement(s, toOhmsBefore.applyAsDouble(s)),
            new TetrapolarMeasurement(s, toOhmsAfter.applyAsDouble(s)),
            dh)
        )
        .map(Measurement.class::cast).toList();
  }
}
