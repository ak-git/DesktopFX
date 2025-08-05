package com.ak.rsm.system;

import com.ak.util.Metrics;

import javax.measure.MetricPrefix;
import java.util.Collection;
import java.util.Objects;

import static tech.units.indriya.unit.Units.METRE;

public record TetrapolarSystem(double sPU, double lCC) {
  public double factor(double sign) {
    return Math.abs(lCC + Math.signum(sign) * sPU) / 2.0;
  }

  public double getDim() {
    return l();
  }

  public RelativeTetrapolarSystem relativeSystem() {
    return new RelativeTetrapolarSystem(sPU / lCC);
  }

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
    return "%6.3f x %6.3f %s".formatted(
        Metrics.Length.METRE.to(sPU, MetricPrefix.MILLI(METRE)),
        Metrics.Length.METRE.to(lCC, MetricPrefix.MILLI(METRE)),
        MetricPrefix.MILLI(METRE)
    );
  }

  private double s() {
    return Math.min(sPU, lCC);
  }

  private double l() {
    return Math.max(sPU, lCC);
  }
}

