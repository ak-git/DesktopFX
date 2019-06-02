package com.ak.rsm;

import javax.annotation.Nonnegative;

import org.apache.commons.math3.analysis.UnivariateFunction;

final class Potential1Layer extends AbstractPotentialLayer implements UnivariateFunction {
  Potential1Layer(double r) {
    super(r);
  }

  @Override
  public double value(@Nonnegative double rho) {
    return value(rho, r -> 1.0 / r);
  }
}
