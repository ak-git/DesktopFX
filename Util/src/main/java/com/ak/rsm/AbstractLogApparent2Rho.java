package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;

import org.apache.commons.math3.analysis.UnivariateFunction;

abstract class AbstractLogApparent2Rho extends AbstractApparent implements UnivariateFunction {
  AbstractLogApparent2Rho(@Nonnegative double sToL, @Nonnegative double Lh) {
    super(sToL, Lh);
  }

  @Override
  public final double value(double k) {
    if (Double.compare(k, 0.0) == 0 || Double.isInfinite(Lh())) {
      return 0.0;
    }
    else {
      DoubleBinaryOperator sum = sum();
      return innerValue(Layers.sum(n -> commonFactor(k, n) * (sum.applyAsDouble(-1.0, n) - sum.applyAsDouble(1.0, n))));
    }
  }

  abstract double innerValue(double sums);

  abstract double commonFactor(double k, @Nonnegative int n);

  abstract DoubleBinaryOperator sum();
}
