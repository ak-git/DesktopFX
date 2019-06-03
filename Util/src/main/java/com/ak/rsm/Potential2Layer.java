package com.ak.rsm;

import javax.annotation.Nonnegative;

import org.apache.commons.math3.analysis.TrivariateFunction;

import static java.lang.StrictMath.pow;

final class Potential2Layer extends AbstractPotentialLayer implements TrivariateFunction {
  Potential2Layer(double r) {
    super(r);
  }

  @Override
  public double value(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double h) {
    return value(rho1, r -> (1.0 / r + 2.0 * Layers.sum(n -> pow(Layers.getK12(rho1, rho2), n), Layers.denominator(r, h))));
  }
}
