package com.ak.rsm;

import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnegative;

abstract class AbstractLogApparent3Rho extends AbstractApparent {
  private final double Lh;

  AbstractLogApparent3Rho(@Nonnegative double sToL, @Nonnegative double Lh) {
    super(sToL);
    this.Lh = Lh;
  }

  final double value(double k12, double k23, @Nonnegative int p1, @Nonnegative int p2mp1) {
    double[] q = Layers.qn(k12, k23, p1, p2mp1);
    IntToDoubleFunction sum = sum(q);
    return innerValue(sum.applyAsDouble(-1) - sum.applyAsDouble(1));
  }

  final double Lh() {
    return Lh;
  }

  abstract double innerValue(double sums);

  abstract IntToDoubleFunction sum(double[] q);
}
