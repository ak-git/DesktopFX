package com.ak.rsm;

import javax.annotation.Nonnegative;

import static java.lang.StrictMath.hypot;

final class Potential2Layer extends AbstractPotentialLayer {
  Potential2Layer(double r) {
    super(r);
  }

  double value(@Nonnegative int n, @Nonnegative double h) {
    return value(r -> 1.0 / hypot(r, 2.0 * n * h));
  }
}
