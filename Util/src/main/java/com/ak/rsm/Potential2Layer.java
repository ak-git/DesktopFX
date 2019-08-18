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
    return value(rho1, r -> {
      double k = Layers.getK12(rho1, rho2);
      double result = 1.0 / r;
      if (Double.compare(k, 0.0) != 0.0) {
        result += 2.0 * Layers.sum(n -> pow(k, n), Layers.denominator(r, h));
      }
      return result;
    });
  }
}
