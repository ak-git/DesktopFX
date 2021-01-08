package com.ak.rsm;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;

import com.ak.util.Metrics;
import com.ak.util.Strings;

@ThreadSafe
final class TetrapolarMeasurement implements Measurement {
  @Nonnull
  private final InexactTetrapolarSystem system;
  @Nonnegative
  private final double resistivity;

  TetrapolarMeasurement(@Nonnull InexactTetrapolarSystem system, @Nonnegative double rOhms) {
    this.system = system;
    resistivity = system.toExact().getApparent(rOhms);
  }

  @Override
  @Nonnull
  public InexactTetrapolarSystem getSystem() {
    return system;
  }

  @Override
  @Nonnegative
  public double getResistivity() {
    return resistivity;
  }

  @Override
  public String toString() {
    return "%s; %s (%.0f %%)".formatted(String.valueOf(system),
        Strings.rho(resistivity, system.getDeltaApparent()),
        Metrics.toPercents(system.getDeltaApparent()));
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static Collection<Measurement> of(InexactTetrapolarSystem[] systems, double[] ohms) {
    return IntStream.range(0, systems.length)
        .mapToObj(i -> new TetrapolarMeasurement(systems[i], ohms[i]))
        .collect(Collectors.toUnmodifiableList());
  }
}
