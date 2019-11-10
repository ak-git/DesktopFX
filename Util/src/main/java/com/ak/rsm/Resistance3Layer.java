package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>3-layer</b> model.
 */
final class Resistance3Layer extends AbstractResistanceLayer<Potential3Layer> {
  @Nonnull
  private final Resistance1Layer resistance1Layer;
  @Nonnull
  private final Resistance2Layer resistance2Layer;
  @Nonnegative
  private final double hStep;

  Resistance3Layer(@Nonnull TetrapolarSystem electrodeSystem, double hStep) {
    super(electrodeSystem, value -> new Potential3Layer(value, hStep));
    resistance1Layer = new Resistance1Layer(electrodeSystem);
    resistance2Layer = new Resistance2Layer(electrodeSystem);
    this.hStep = Math.abs(hStep);
  }

  /**
   * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm)
   *
   * @param rho1  specific resistance of <b>1st-layer</b> in Ohm-m
   * @param rho2  specific resistance of <b>2nd-layer</b> in Ohm-m
   * @param rho3  specific resistance of <b>3nd-layer</b> in Ohm-m
   * @param p1    height of <b>1-layer</b>
   * @param p2mp1 height of <b>2-layer</b>
   * @return resistance R<sub>m-n</sub> (in Ohm)
   */
  public double value(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double rho3, @Nonnegative int p1, @Nonnegative int p2mp1) {
    if (Double.compare(rho1, rho2) != 0 && Double.compare(rho2, rho3) != 0 && p1 > 0 && p2mp1 > 0) {
      double k12 = Layers.getK12(rho1, rho2);
      double k23 = Layers.getK12(rho2, rho3);
      double[] q = Layers.qn(k12, k23, p1, p2mp1);
      double r3 = resistance1Layer.value(rho1) + (2.0 * rho1 / Math.PI) * Layers.sum(n -> q[n] * apply(r -> r.value(n)));
      if (k12 < 0 && k23 > 0) {
        return Math.max(resistance2Layer.value(rho1, rho2, hStep * p1), r3);
      }
      else {
        return r3;
      }
    }
    else if (p1 < 1 && p2mp1 < 1) {
      return resistance1Layer.value(rho3);
    }
    else if (Double.compare(rho1, rho2) != 0 && p1 > 0) {
      return resistance2Layer.value(rho1, rho2, hStep * p1);
    }
    else if (Double.compare(rho2, rho3) != 0) {
      return resistance2Layer.value(rho2, rho3, hStep * (p2mp1 + p1));
    }
    else {
      return resistance1Layer.value(rho1);
    }
  }

  @Nonnull
  public static Medium inverse(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
    return Resistance1Layer.inverse(systems, rOhmsBefore);
  }
}
