package com.ak.rsm;

import javax.annotation.Nonnegative;

import org.apache.commons.math3.analysis.TrivariateFunction;

import static java.lang.StrictMath.pow;

final class DerivatePotential2LayerByH extends AbstractPotentialLayer implements TrivariateFunction {
  DerivatePotential2LayerByH(double r) {
    super(r);
  }

  @Override
  public double value(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double h) {
    return value(rho1, r -> {
      double k = Layers.getK12(rho1, rho2);
      if (Double.compare(k, 0.0) == 0) {
        return 0.0;
      }
      else {
        return -8.0 * h * Layers.sum(
            n -> pow(n, 2.0) * pow(k, n),
            n -> pow(Layers.denominator(r, h).applyAsDouble(n), 3.0));
      }
    });
  }
}
