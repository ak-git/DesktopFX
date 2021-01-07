package com.ak.rsm;

import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class InexactTetrapolarSystem {
  @Nonnegative
  private final double absError;
  @Nonnull
  private final TetrapolarSystem system;

  private InexactTetrapolarSystem(@Nonnegative double absError, @Nonnull TetrapolarSystem system) {
    this.absError = absError;
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

  static Builder si(@Nonnegative double absError) {
    return new Builder(DoubleUnaryOperator.identity(), absError);
  }

  static class Builder extends TetrapolarSystem.AbstractBuilder<InexactTetrapolarSystem> {
    @Nonnegative
    private final double absError;

    protected Builder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absErrorMilli) {
      super(converter);
      absError = converter.applyAsDouble(absErrorMilli);
    }

    public final Builder s(@Nonnegative double smm) {
      s = converter.applyAsDouble(smm);
      return this;
    }

    @Override
    public InexactTetrapolarSystem l(@Nonnegative double lmm) {
      return new InexactTetrapolarSystem(absError, TetrapolarSystem.si().s(s).l(converter.applyAsDouble(lmm)));
    }
  }
}
