package com.ak.rsm;

import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Strings;

record TetrapolarDerivativeMeasurement(@Nonnull Measurement measurement, @Nonnull double derivativeResistivity)
    implements DerivativeMeasurement {
  TetrapolarDerivativeMeasurement(@Nonnull Measurement measurement,
                                  @Nonnull Measurement measurementAfter, double dh) {
    this(measurement, (measurementAfter.resistivity() - measurement.resistivity()) / (dh / measurement.system().getL()));
  }

  @Nonnull
  @Override
  public TetrapolarSystem system() {
    return measurement.system();
  }

  @Nonnegative
  @Override
  public double resistivity() {
    return measurement.resistivity();
  }

  @Override
  @Nonnull
  public Prediction toPrediction(@Nonnull RelativeMediumLayers kw, @Nonnegative double rho1) {
    return new TetrapolarDerivativePrediction(system(), kw, rho1, new double[] {resistivity(), derivativeResistivity()});
  }

  @Override
  public String toString() {
    return "%s, %s".formatted(measurement, Strings.dRhoByPhi(derivativeResistivity));
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static List<DerivativeMeasurement> of(TetrapolarSystem[] systems, double[] rOhmsBefore, double[] rOhmsAfter, double dh) {
    var rOhmsBeforeIt = DoubleStream.of(rOhmsBefore).iterator();
    var rOhmsAfterIt = DoubleStream.of(rOhmsAfter).iterator();

    return Arrays.stream(systems)
        .map(s ->
            new TetrapolarDerivativeMeasurement(
                new TetrapolarMeasurement(s, s.getApparent(rOhmsBeforeIt.nextDouble())),
                new TetrapolarMeasurement(s, s.getApparent(rOhmsAfterIt.nextDouble())),
                dh
            )
        )
        .map(DerivativeMeasurement.class::cast)
        .toList();
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static List<DerivativeMeasurement> of(TetrapolarSystem[] systems, double[] resistivity, double[] resistivityDiff) {
    var rIt = DoubleStream.of(resistivity).iterator();
    var rDiffIt = DoubleStream.of(resistivityDiff).iterator();
    return Arrays.stream(systems)
        .map(s ->
            new TetrapolarDerivativeMeasurement(
                new TetrapolarMeasurement(s, rIt.nextDouble()), rDiffIt.nextDouble()
            )
        )
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
        .map(s ->
            new TetrapolarDerivativeMeasurement(
                new TetrapolarMeasurement(s, s.getApparent(toOhmsBefore.applyAsDouble(s))),
                new TetrapolarMeasurement(s, s.getApparent(toOhmsAfter.applyAsDouble(s))),
                dh
            )
        )
        .map(Measurement.class::cast).toList();
  }
}
