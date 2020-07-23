package com.ak.rsm;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Strings;

final class TetrapolarMeasurement implements Measurement {
  @Nonnull
  private final TetrapolarSystem system;
  @Nonnegative
  private final double resistivity;

  TetrapolarMeasurement(@Nonnull TetrapolarSystem system, @Nonnegative double rOhms) {
    this.system = system;
    resistivity = system.getApparent(rOhms);
  }

  @Override
  public double getResistivity() {
    return resistivity;
  }

  @Override
  public double getLogResistivity() {
    return StrictMath.log(resistivity);
  }

  @Override
  public TetrapolarSystem getSystem() {
    return system;
  }

  @Override
  public String toString() {
    return String.format("%s; meas \u03c1 = %.3f %s", system, resistivity, Strings.OHM_METRE);
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static Collection<Measurement> of(TetrapolarSystem[] systems, double[] ohms) {
    return IntStream.range(0, systems.length)
        .mapToObj(i -> new TetrapolarMeasurement(systems[i], ohms[i]))
        .collect(Collectors.toUnmodifiableList());
  }
}
