package com.ak.rsm;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;

public final class TetrapolarSystem {
  @Nonnegative
  private final double sPU;
  @Nonnegative
  private final double lCC;
  @Nonnull
  private final RelativeTetrapolarSystem relativeSystem;

  private TetrapolarSystem(@Nonnegative double sPU, @Nonnegative double lCC) {
    this.sPU = Math.abs(sPU);
    this.lCC = Math.abs(lCC);
    relativeSystem = new RelativeTetrapolarSystem(sPU / lCC);
  }

  TetrapolarSystem shift(double deltaS, double deltaL) {
    return new TetrapolarSystem(sPU + deltaS, lCC + deltaL);
  }

  @Nonnull
  RelativeTetrapolarSystem toRelative() {
    return relativeSystem;
  }

  double factor(double sign) {
    return Math.abs(lCC + Math.signum(sign) * sPU) / 2.0;
  }

  double getL() {
    return lCC;
  }

  /**
   * Gets <b>apparent</b> specific resistance which is correspond to 1-layer model.
   *
   * @param rOhms in Ohms.
   * @return <b>apparent</b> specific resistance in Ohm-m.
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
    if (!(o instanceof TetrapolarSystem)) {
      return false;
    }

    TetrapolarSystem that = (TetrapolarSystem) o;
    return Double.compare(Math.min(sPU, lCC), Math.min(that.sPU, that.lCC)) == 0 &&
        Double.compare(Math.max(sPU, lCC), Math.max(that.sPU, that.lCC)) == 0;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new double[] {Math.min(sPU, lCC), Math.max(sPU, lCC)});
  }

  @Override
  public String toString() {
    return "%2.0f x %2.0f %s".formatted(Metrics.toMilli(sPU), Metrics.toMilli(lCC), MetricPrefix.MILLI(METRE));
  }

  public static Builder milli() {
    return new Builder(Metrics.MILLI);
  }

  static Builder si() {
    return new Builder(DoubleUnaryOperator.identity());
  }

  abstract static class AbstractBuilder<T> {
    @Nonnull
    final DoubleUnaryOperator converter;
    @Nonnegative
    double s;

    protected AbstractBuilder(@Nonnull DoubleUnaryOperator converter) {
      this.converter = converter;
    }

    public abstract T l(@Nonnegative double l);
  }

  public static class Builder extends AbstractBuilder<TetrapolarSystem> {
    public Builder(@Nonnull DoubleUnaryOperator converter) {
      super(converter);
    }

    public final Builder s(@Nonnegative double s) {
      this.s = converter.applyAsDouble(s);
      return this;
    }

    @Override
    public TetrapolarSystem l(@Nonnegative double l) {
      return new TetrapolarSystem(s, converter.applyAsDouble(l));
    }
  }
}

