package com.ak.rsm;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import tec.uom.se.unit.Units;

final class TetrapolarDerivativeMeasurement implements DerivativeMeasurement {
  @Nonnull
  private final Measurement measurement;
  @Nonnegative
  private final double dRhoBydH;

  TetrapolarDerivativeMeasurement(@Nonnull Measurement measurementBefore,
                                  @Nonnull Measurement measurementAfter, double dh) {
    measurement = measurementBefore;
    dRhoBydH = (measurementAfter.getResistivity() - measurement.getResistivity()) / dh;
  }

  @Override
  public double getResistivity() {
    return measurement.getResistivity();
  }

  @Override
  public double getLogResistivity() {
    return measurement.getLogResistivity();
  }

  @Override
  public double getDerivativeResistivity() {
    return dRhoBydH;
  }

  @Override
  public double getDerivativeLogResistivity() {
    return StrictMath.log(Math.abs(dRhoBydH));
  }

  @Override
  public TetrapolarSystem getSystem() {
    return measurement.getSystem();
  }

  @Override
  public String toString() {
    return String.format("%s, d\u03c1/dh = %.0f %s", measurement, dRhoBydH, Units.OHM);
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static Collection<DerivativeMeasurement> of(TetrapolarSystem[] systems, double[] rOhmsBefore, double[] rOhmsAfter, double dh) {
    return IntStream.range(0, systems.length)
        .mapToObj(i -> new TetrapolarDerivativeMeasurement(
            new TetrapolarMeasurement(systems[i], rOhmsBefore[i]),
            new TetrapolarMeasurement(systems[i], rOhmsAfter[i]),
            dh
        ))
        .collect(Collectors.toUnmodifiableList());
  }
}
