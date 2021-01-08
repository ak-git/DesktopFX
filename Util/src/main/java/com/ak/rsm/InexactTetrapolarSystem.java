package com.ak.rsm;

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
  @Nonnegative
  private final double relError;
  @Nonnull
  private final TetrapolarSystem system;

  private InexactTetrapolarSystem(@Nonnegative double absError, @Nonnegative double maxLen, @Nonnull TetrapolarSystem system) {
    this.absError = absError;
    relError = absError / maxLen;
    this.system = system;
  }

  @Nonnull
  TetrapolarSystem getSystem() {
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
    return system.toRelative().errorFactor() * relError;
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

  static Builder milli(@Nonnegative double absError) {
    return new Builder(Metrics.MILLI, absError);
  }

  static Builder si(@Nonnegative double absError) {
    return new Builder(DoubleUnaryOperator.identity(), absError);
  }

  static class Builder extends TetrapolarSystem.AbstractBuilder<InexactTetrapolarSystem> {
    @Nonnegative
    private final double absError;

    protected Builder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
      super(converter);
      this.absError = converter.applyAsDouble(absError);
    }

    public final Builder s(@Nonnegative double s) {
      this.s = converter.applyAsDouble(s);
      return this;
    }

    @Override
    public InexactTetrapolarSystem l(@Nonnegative double l) {
      double lCC = converter.applyAsDouble(l);
      return new InexactTetrapolarSystem(absError, Math.max(s, lCC), TetrapolarSystem.si().s(s).l(lCC));
    }
  }
}
