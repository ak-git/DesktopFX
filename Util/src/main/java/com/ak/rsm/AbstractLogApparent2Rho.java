package com.ak.rsm;

import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnegative;

import org.apache.commons.math3.analysis.BivariateFunction;

abstract class AbstractLogApparent2Rho extends AbstractApparent implements BivariateFunction {
  AbstractLogApparent2Rho(@Nonnegative double sToL) {
    super(sToL);
  }

  @Override
  public final double value(double k, @Nonnegative double Lh) {
    IntToDoubleFunction sum = sum(k, Lh);
    return innerValue(Lh, sum.applyAsDouble(-1) - sum.applyAsDouble(1));
  }

  abstract double innerValue(double Lh, double sums);

  abstract IntToDoubleFunction sum(double k, double Lh);
}
