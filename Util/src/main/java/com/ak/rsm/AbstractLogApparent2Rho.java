package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractLogApparent2Rho extends AbstractApparent {
  AbstractLogApparent2Rho(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  public final double value(double k, @Nonnegative double h) {
    if (Double.compare(k, 0.0) == 0 || Double.compare(h, 0.0) == 0) {
      return 0.0;
    }
    else {
      DoubleBinaryOperator sum = sum(h);
      return innerValue(Layers.sum(n -> StrictMath.pow(k, n) * commonFactor(n) * (sum.applyAsDouble(-1.0, n) - sum.applyAsDouble(1.0, n))));
    }
  }

  abstract double innerValue(double sums);

  abstract int commonFactor(@Nonnegative int n);

  abstract DoubleBinaryOperator sum(@Nonnegative double h);
}
