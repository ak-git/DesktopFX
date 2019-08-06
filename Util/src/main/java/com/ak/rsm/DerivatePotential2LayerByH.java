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
    return value(rho1, r -> -8.0 * h * Layers.sum(
        n -> pow(n, 2.0) * pow(Layers.getK12(rho1, rho2), n),
        n -> pow(Layers.denominator(r, h).applyAsDouble(n), 3.0))
    );
  }
}
