package com.ak.rsm;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

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
  double getAbsError() {
    return absError;
  }

  @Nonnull
  TetrapolarSystem toExact() {
    return system;
  }

  @Nonnull
  TetrapolarSystem shift(int signS, int signL) {
    return system.shift(Math.signum(signS) * absError, Math.signum(signL) * absError);
  }

  /**
   * dRho / Rho = E * dL / L
   *
   * @return relative apparent error
   */
  @Nonnegative
  double getDeltaApparent() {
    return system.toRelative().errorFactor() * system.getRelativeErrorL(absError);
  }

  @Override
  public String toString() {
    return "%s / %.1f %s".formatted(system.toString(), Metrics.toMilli(absError), MetricPrefix.MILLI(METRE));
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

  @Nonnull
  private static InexactTetrapolarSystem[] toInexactMilli(@Nonnegative double absErrorL, @Nonnull TetrapolarSystem[] systems) {
    return Arrays.stream(systems)
        .map(system -> new InexactTetrapolarSystem(Metrics.fromMilli(absErrorL), system))
        .toArray(InexactTetrapolarSystem[]::new);
  }

  static Builder milli(@Nonnegative double absError) {
    return new Builder(Metrics.MILLI, absError);
  }

  static Builder si(@Nonnegative double absError) {
    return new Builder(DoubleUnaryOperator.identity(), absError);
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
