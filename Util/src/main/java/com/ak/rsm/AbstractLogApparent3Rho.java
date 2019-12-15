package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractLogApparent3Rho extends AbstractApparent {
  AbstractLogApparent3Rho(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  final double value(double k12, double k23, @Nonnegative double h, @Nonnegative int p1, @Nonnegative int p2mp1) {
    double[] q = Layers.qn(k12, k23, p1, p2mp1);
    DoubleBinaryOperator sum = sum(q, h);
    return innerValue(Layers.sum(n -> q[n] * (sum.applyAsDouble(-1.0, n) - sum.applyAsDouble(1.0, n))));
  }

  abstract double innerValue(double sums);

  abstract DoubleBinaryOperator sum(@Nonnull double[] q, @Nonnegative double h);
}
