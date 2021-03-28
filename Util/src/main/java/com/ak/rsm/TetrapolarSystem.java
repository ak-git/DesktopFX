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

  @Nonnull
  TetrapolarSystem shift(double deltaS, double deltaL) {
    return new TetrapolarSystem(sPU + deltaS, lCC + deltaL);
  }

  @Nonnull
  RelativeTetrapolarSystem toRelative() {
    return relativeSystem;
  }

  @Nonnegative
  double getLRelativeError(@Nonnegative double absErrorL) {
    return absErrorL / l();
  }

  @Nonnegative
  double getHMax(double k, @Nonnegative double absErrorL) {
    return relativeSystem.hMaxFactor(k) * l() / StrictMath.pow(getLRelativeError(absErrorL), 1.0 / 3.0);
  }

  @Nonnegative
  double getHMin(double k, @Nonnegative double absErrorL) {
    return l() * Math.sqrt(getLRelativeError(absErrorL)) * relativeSystem.hMinFactor(k);
  }

  @Nonnegative
  double factor(double sign) {
    return Math.abs(lCC + Math.signum(sign) * sPU) / 2.0;
  }

  @Nonnegative
  double getS() {
    return sPU;
  }

  @Nonnegative
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
    return Double.compare(s(), that.s()) == 0 && Double.compare(l(), that.l()) == 0;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new double[] {s(), l()});
  }

  @Override
  public String toString() {
    return "%2.3f x %2.3f %s".formatted(Metrics.toMilli(sPU), Metrics.toMilli(lCC), MetricPrefix.MILLI(METRE));
  }

  private double s() {
    return Math.min(sPU, lCC);
  }

  private double l() {
    return Math.max(sPU, lCC);
  }

  /**
   * Generates optimal electrode system pair.
   * <p>
   * For 10 mm: <b>10 x 30, 50 x 30 mm</b>
   * </p>
   *
   * @return two Tetrapolar System.
   */
  @Nonnull
  static TetrapolarSystem[] systems2(@Nonnegative double smm) {
    return new TetrapolarSystem[] {
        milli().s(smm).l(smm * 3.0),
        milli().s(smm * 5.0).l(smm * 3.0),
    };
  }

  /**
   * Generates optimal electrode system pair.
   * <p>
   * For 10 mm: <b>10 x 30, 50 x 30, 20 x 40, 60 x 40 mm</b>
   * </p>
   * <p>
   * For 7 mm: <b>7 x 21, 35 x 21, 14 x 28, 42 x 28 mm</b>
   * </p>
   *
   * @param smm small potential electrode distance, mm.
   * @return three Tetrapolar System.
   */
  @Nonnull
  static TetrapolarSystem[] systems4(@Nonnegative double smm) {
    return new TetrapolarSystem[] {
        milli().s(smm).l(smm * 3.0),
        milli().s(smm * 5.0).l(smm * 3.0),
        milli().s(smm * 2.0).l(smm * 4.0),
        milli().s(smm * 6.0).l(smm * 4.0),
    };
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

    abstract T l(@Nonnegative double l);
  }

  public static class Builder extends AbstractBuilder<TetrapolarSystem> {
    private Builder(@Nonnull DoubleUnaryOperator converter) {
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

