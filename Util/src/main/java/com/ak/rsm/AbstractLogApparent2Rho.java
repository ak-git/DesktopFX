package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;

import org.apache.commons.math3.analysis.BivariateFunction;

abstract class AbstractLogApparent2Rho extends AbstractApparent implements BivariateFunction {
  AbstractLogApparent2Rho(@Nonnegative double sToL) {
    super(sToL);
  }

  @Override
  public final double value(double k, @Nonnegative double Lh) {
    if (Double.compare(k, 0.0) == 0) {
      return 0.0;
    }
    else {
      DoubleBinaryOperator sum = sum(Lh);
      return innerValue(Lh, Layers.sum(n -> commonFactor(k, n) * (sum.applyAsDouble(-1.0, n) - sum.applyAsDouble(1.0, n))));
    }
  }

  abstract double innerValue(@Nonnegative double Lh, double sums);

  abstract double commonFactor(double k, @Nonnegative int n);

  abstract DoubleBinaryOperator sum(@Nonnegative double Lh);
}
