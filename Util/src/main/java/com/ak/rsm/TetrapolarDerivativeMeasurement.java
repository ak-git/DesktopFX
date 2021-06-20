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
  @Nonnull
  private final Measurement measurementAfter;
  private final double dh;
  private final double dRhoBydPhi;

  TetrapolarDerivativeMeasurement(@Nonnull Measurement measurementBefore,
                                  @Nonnull Measurement measurementAfter, double dh) {
    measurement = measurementBefore;
    this.measurementAfter = measurementAfter;
    this.dh = dh;
    dRhoBydPhi = (measurementAfter.getResistivity() - measurement.getResistivity()) / (dh / getSystem().getL());
  }

  @Override
  public double getR() {
    return measurement.getR();
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

  @Nonnull
  @Override
  public Measurement newInstance(@Nonnull TetrapolarSystem system) {
    return new TetrapolarDerivativeMeasurement(measurement.newInstance(system), measurementAfter.newInstance(system), dh);
  }

  @Override
  public String toString() {
    return "%s, %s".formatted(String.valueOf(measurement), Strings.dRhoByPhi(dRhoBydPhi));
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
}
