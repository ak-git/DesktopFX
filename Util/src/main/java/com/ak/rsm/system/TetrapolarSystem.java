package com.ak.rsm.system;

import com.ak.util.Metrics;
import tec.uom.se.unit.MetricPrefix;

import javax.annotation.Nonnegative;
import java.util.Collection;
import java.util.Objects;

import static tec.uom.se.unit.Units.METRE;

public record TetrapolarSystem(@Nonnegative double sPU, @Nonnegative double lCC) {
  @Nonnegative
  public double factor(double sign) {
    return Math.abs(lCC + Math.signum(sign) * sPU) / 2.0;
  }

  @Nonnegative
  public double getDim() {
    return l();
  }

  public RelativeTetrapolarSystem relativeSystem() {
    return new RelativeTetrapolarSystem(sPU / lCC);
  }

  @Nonnegative
  public static double getBaseL(Collection<TetrapolarSystem> systems) {
    return systems.stream().mapToDouble(TetrapolarSystem::lCC).max().orElseThrow();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TetrapolarSystem that)) {
      return false;
    }
    return Double.compare(that.s(), s()) == 0 && Double.compare(that.l(), l()) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(s(), l());
  }

  @Override
  public String toString() {
    return "%2.3f x %2.3f %s".formatted(
        Metrics.Length.METRE.to(sPU, MetricPrefix.MILLI(METRE)),
        Metrics.Length.METRE.to(lCC, MetricPrefix.MILLI(METRE)),
        MetricPrefix.MILLI(METRE)
    );
  }

  @Nonnegative
  private double s() {
    return Math.min(sPU, lCC);
  }

  @Nonnegative
  private double l() {
    return Math.max(sPU, lCC);
  }
}

