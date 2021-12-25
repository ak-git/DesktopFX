package com.ak.rsm.system;

import java.util.Objects;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;

public record TetrapolarSystem(@Nonnegative double sPU, @Nonnegative double lCC,
                               @Nonnull RelativeTetrapolarSystem relativeSystem) {
  public TetrapolarSystem(@Nonnegative double sPU, @Nonnegative double lCC) {
    this(Math.abs(sPU), Math.abs(lCC), new RelativeTetrapolarSystem(sPU / lCC));
  }

  @Nonnegative
  public double factor(double sign) {
    return Math.abs(lCC + Math.signum(sign) * sPU) / 2.0;
  }

  @Nonnegative
  public double getDim() {
    return l();
  }

  /**
   * Gets <b>apparent</b> specific ohms which is correspond to 1-layer model.
   *
   * @param rOhms in Ohms.
   * @return <b>apparent</b> specific ohms in Ohm-m.
   */
  @Nonnegative
  public double getApparent(@Nonnegative double rOhms) {
    return rOhms * Math.PI / (Math.abs(1.0 / factor(-1.0)) - Math.abs(1.0 / factor(1.0)));
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
    return "%2.3f x %2.3f %s".formatted(Metrics.toMilli(sPU), Metrics.toMilli(lCC), MetricPrefix.MILLI(METRE));
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

