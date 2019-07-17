package com.ak.rsm;

import javax.annotation.Nonnegative;

final class Potential3Layer extends AbstractPotentialLayer {
  @Nonnegative
  private final double hStep;

  Potential3Layer(double r, @Nonnegative double hStep) {
    super(r);
    this.hStep = hStep;
  }

  /**
   * Calculates potential U(r)
   *
   * @param rho1  specific resistance of <b>1st-layer</b> in Ohm-m
   * @param rho2  specific resistance of <b>2nd-layer</b> in Ohm-m
   * @param rho3  specific resistance of <b>3nd-layer</b> in Ohm-m
   * @param p1    height of <b>1-layer</b>
   * @param p2mp1 height of <b>2-layer</b>
   * @return resistance R<sub>m-n</sub> (in Ohm)
   */
  public double value(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double rho3, @Nonnegative int p1, @Nonnegative int p2mp1) {
    double k12 = Layers.getK12(rho1, rho2);
    double k23 = Layers.getK12(rho2, rho3);
    double[] q = Layers.qn(k12, k23, p1, p2mp1);
    return value(rho1, r -> (1.0 / r + 2.0 * Layers.sum(n -> q[n], Layers.denominator(r, hStep))));
  }
}
