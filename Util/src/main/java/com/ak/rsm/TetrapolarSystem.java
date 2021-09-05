package com.ak.rsm;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.ToLongFunction;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

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
  @Nonnegative
  private final double absError;
  @Nonnull
  private final RelativeTetrapolarSystem relativeSystem;

  private TetrapolarSystem(@Nonnegative double absError, @Nonnegative double sPU, @Nonnegative double lCC) {
    this.absError = Math.abs(absError);
    this.sPU = Math.abs(sPU);
    this.lCC = Math.abs(lCC);
    relativeSystem = new RelativeTetrapolarSystem(sPU / lCC);
  }

  @Nonnegative
  double getAbsError() {
    return absError;
  }

  @Nonnull
  RelativeTetrapolarSystem toRelative() {
    return relativeSystem;
  }

  @Nonnegative
  double getHMax(double k) {
    return relativeSystem.hMaxFactor(k) * l() / StrictMath.pow(getLRelativeError(), 1.0 / 3.0);
  }

  @Nonnegative
  double getHMin(double k) {
    return l() * Math.sqrt(getLRelativeError()) * relativeSystem.hMinFactor(k);
  }

  /**
   * dRho / Rho = E * dL / L
   *
   * @return relative apparent error
   */
  @Nonnegative
  double getApparentRelativeError() {
    return Math.abs(relativeSystem.errorFactor() * getLRelativeError());
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
    if (!(o instanceof TetrapolarSystem that)) {
      return false;
    }
    return Double.compare(that.s(), s()) == 0 && Double.compare(that.l(), l()) == 0 && Double.compare(that.absError, absError) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(s(), l(), absError);
  }

  @Override
  public String toString() {
    String s = "%2.3f x %2.3f %s".formatted(Metrics.toMilli(sPU), Metrics.toMilli(lCC), MetricPrefix.MILLI(METRE));
    if (absError > 0) {
      return "%s / %.1f %s; \u2195 %.0f %s".formatted(
          s, Metrics.toMilli(absError), MetricPrefix.MILLI(METRE),
          Metrics.toMilli(getHMax(1.0)), MetricPrefix.MILLI(METRE));
    }
    else {
      return s;
    }
  }

  @Nonnull
  static Collection<List<TetrapolarSystem>> getMeasurementsCombination(@Nonnull Collection<TetrapolarSystem> systems) {
    ToLongFunction<Collection<TetrapolarSystem>> distinctSizes =
        ts -> ts.stream().flatMap(s -> DoubleStream.of(s.getS(), s.getL()).boxed()).distinct().count();
    var initialSizes = distinctSizes.applyAsLong(systems);
    return IntStream.range(0, 1 << systems.size())
        .mapToObj(n -> {
          var signIndex = new AtomicInteger();
          IntUnaryOperator sign = index -> (n & (1 << index)) == 0 ? 1 : -1;
          return systems.stream().map(s -> s.shift(sign.applyAsInt(signIndex.getAndIncrement()))).toList();
        })
        .filter(s -> initialSizes == distinctSizes.applyAsLong(s)).toList();
  }

  @Nonnull
  private TetrapolarSystem shift(int sign) {
    double err = Math.signum(sign) * absError;
    if (sPU < lCC) {
      err *= -1.0;
    }
    return new TetrapolarSystem(absError, sPU + err, lCC - err);
  }

  private double s() {
    return Math.min(sPU, lCC);
  }

  private double l() {
    return Math.max(sPU, lCC);
  }

  @Nonnegative
  double getLRelativeError() {
    return absError / l();
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
  static TetrapolarSystem[] systems2(@Nonnegative double absErrorMilli, @Nonnegative double smm) {
    return new TetrapolarSystem[] {
        milli(absErrorMilli).s(smm).l(smm * 3.0),
        milli(absErrorMilli).s(smm * 5.0).l(smm * 3.0),
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
  static TetrapolarSystem[] systems4(@Nonnegative double absErrorMilli, @Nonnegative double smm) {
    return new TetrapolarSystem[] {
        milli(absErrorMilli).s(smm).l(smm * 3.0),
        milli(absErrorMilli).s(smm * 5.0).l(smm * 3.0),
        milli(absErrorMilli).s(smm * 2.0).l(smm * 4.0),
        milli(absErrorMilli).s(smm * 6.0).l(smm * 4.0),
    };
  }

  public static Builder milli(@Nonnegative double absErrorMilli) {
    return new Builder(Metrics.MILLI, absErrorMilli);
  }

  static Builder si(@Nonnegative double absError) {
    return new Builder(DoubleUnaryOperator.identity(), absError);
  }

  public static class Builder {
    @Nonnull
    private final DoubleUnaryOperator converter;
    @Nonnegative
    private final double absError;
    @Nonnegative
    private double s;

    private Builder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
      this.converter = converter;
      this.absError = absError;
    }

    public Builder s(@Nonnegative double s) {
      this.s = s;
      return this;
    }

    public TetrapolarSystem l(@Nonnegative double l) {
      return new TetrapolarSystem(converter.applyAsDouble(absError), converter.applyAsDouble(s), converter.applyAsDouble(l));
    }
  }
}

