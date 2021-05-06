package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;

final class InexactTetrapolarSystem {
  @Nonnegative
  private final double absError;
  @Nonnull
  private final TetrapolarSystem system;

  private InexactTetrapolarSystem(@Nonnegative double absError, @Nonnull TetrapolarSystem system) {
    this.absError = Math.abs(absError);
    this.system = system;
  }

  @Nonnegative
  double getHMax(double k) {
    return system.getHMax(k, absError);
  }

  @Nonnegative
  double getHMin(double k) {
    return system.getHMin(k, absError);
  }

  @Nonnegative
  double getAbsError() {
    return absError;
  }

  @Nonnull
  TetrapolarSystem toExact() {
    return system;
  }

  @Nonnull
  InexactTetrapolarSystem shift(int signS, int signL) {
    return new InexactTetrapolarSystem(absError, system.shift(Math.signum(signS) * absError, Math.signum(signL) * absError));
  }

  /**
   * dRho / Rho = E * dL / L
   *
   * @return relative apparent error
   */
  @Nonnegative
  double getApparentRelativeError() {
    return system.toRelative().errorFactor() * system.getLRelativeError(absError);
  }

  @Override
  public String toString() {
    return "%s / %.1f %s; \u2195 %.0f %s".formatted(
        system.toString(), Metrics.toMilli(absError), MetricPrefix.MILLI(METRE),
        Metrics.toMilli(getHMax(1.0)), MetricPrefix.MILLI(METRE));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !getClass().equals(o.getClass())) {
      return false;
    }
    InexactTetrapolarSystem that = (InexactTetrapolarSystem) o;
    return Double.compare(that.absError, absError) == 0 && system.equals(that.system);
  }

  @Override
  public int hashCode() {
    return Objects.hash(absError, system);
  }

  /**
   * Generates optimal electrode system pair.
   * For 10 mm: 10 x 30, 50 x 30 mm,
   *
   * @return two Inexact Tetrapolar System.
   */
  @Nonnull
  static InexactTetrapolarSystem[] systems2(@Nonnegative double absError, @Nonnegative double smm) {
    return toInexactMilli(absError, TetrapolarSystem.systems2(smm));
  }

  /**
   * Generates optimal electrode system pair.
   * 10 x 30, 30 x 50, 20 x 40, 40 x 60 mm,
   * 7 x 21, 21 x 35, 14 x 28, 28 x 42 mm.
   *
   * @param smm small potential electrode distance, mm.
   * @return three Tetrapolar System.
   */
  @Nonnull
  static InexactTetrapolarSystem[] systems4(@Nonnegative double absError, @Nonnegative double smm) {
    return toInexactMilli(absError, TetrapolarSystem.systems4(smm));
  }

  static Builder milli(@Nonnegative double absError) {
    return new Builder(Metrics.MILLI, absError);
  }

  static Builder si(@Nonnegative double absError) {
    return new Builder(DoubleUnaryOperator.identity(), absError);
  }

  @Nonnull
  static InexactTetrapolarSystem[] toInexact(@Nonnegative double absErrorL, @Nonnull TetrapolarSystem[] systems) {
    return Arrays.stream(systems)
        .map(system -> new InexactTetrapolarSystem(absErrorL, system))
        .toArray(InexactTetrapolarSystem[]::new);
  }

  @Nonnull
  private static InexactTetrapolarSystem[] toInexactMilli(@Nonnegative double absErrorL, @Nonnull TetrapolarSystem[] systems) {
    return toInexact(Metrics.fromMilli(absErrorL), systems);
  }

  @Nonnull
  static Collection<Collection<InexactTetrapolarSystem>> getMeasurementsCombination(@Nonnull List<InexactTetrapolarSystem> systems) {
    ToLongFunction<Collection<InexactTetrapolarSystem>> distinctSizes =
        ts -> ts.stream().flatMap(s -> DoubleStream.of(s.toExact().getS(), s.toExact().getL()).boxed()).distinct().count();
    var initialSizes = distinctSizes.applyAsLong(systems.stream().collect(Collectors.toUnmodifiableList()));
    return IntStream.range(0, 2 << (2 * (systems.size() - 1) + 1))
        .mapToObj(n -> {
          var signIndex = new AtomicInteger();
          IntUnaryOperator sign = index -> (n & (1 << index)) == 0 ? 1 : -1;
          return systems.stream()
              .map(s -> s.shift(
                  sign.applyAsInt(signIndex.getAndIncrement()),
                  sign.applyAsInt(signIndex.getAndIncrement())))
              .collect(Collectors.toUnmodifiableList());
        })
        .filter(s -> initialSizes == distinctSizes.applyAsLong(s))
        .filter(s -> {
          for (var i = 0; i < systems.size(); i++) {
            TetrapolarSystem sBefore = systems.get(i).toExact();
            TetrapolarSystem sAfter = s.get(i).toExact();
            if (Double.compare(Math.signum(sBefore.getS() - sAfter.getS()), Math.signum(sBefore.getL() - sAfter.getL())) != 0) {
              return true;
            }
          }
          return false;
        })
        .collect(Collectors.toUnmodifiableList());
  }

  static class Builder extends TetrapolarSystem.AbstractBuilder<InexactTetrapolarSystem> {
    @Nonnegative
    private final double absError;

    private Builder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
      super(converter);
      this.absError = converter.applyAsDouble(absError);
    }

    final Builder s(@Nonnegative double s) {
      this.s = converter.applyAsDouble(s);
      return this;
    }

    @Override
    InexactTetrapolarSystem l(@Nonnegative double l) {
      return new InexactTetrapolarSystem(absError, TetrapolarSystem.si().s(s).l(converter.applyAsDouble(l)));
    }
  }
}
