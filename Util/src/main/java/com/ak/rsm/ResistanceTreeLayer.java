package com.ak.rsm;

import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.numbers.CoefficientsUtils;
import org.apache.commons.math3.analysis.BivariateFunction;

import static java.lang.StrictMath.hypot;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>3-layer</b> model.
 */
final class ResistanceTreeLayer {
  private static final int INT = 1024 * 8;
  @Nonnull
  private final ResistanceOneLayer resistanceOneLayer;
  private final double hStepSI;

  ResistanceTreeLayer(@Nonnull TetrapolarSystem electrodeSystem, @Nonnegative double hStepSI) {
    resistanceOneLayer = new ResistanceOneLayer(electrodeSystem);
    this.hStepSI = hStepSI;
  }

  /**
   * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm)
   *
   * @param rho1SI specific resistance of <b>1st-layer</b> in Ohm-m
   * @param rho2SI specific resistance of <b>2nd-layer</b> in Ohm-m
   * @param rho3SI specific resistance of <b>3nd-layer</b> in Ohm-m
   * @param p1     height of <b>1-layer</b>
   * @param p2mp1  height of <b>2-layer</b>
   * @return resistance R<sub>m-n</sub> (in Ohm)
   */
  public double value(@Nonnegative double rho1SI, @Nonnegative double rho2SI, @Nonnegative double rho3SI, @Nonnegative int p1, @Nonnegative int p2mp1) {
    double k12 = ResistanceTwoLayer.getK12(rho1SI, rho2SI);
    double k23 = ResistanceTwoLayer.getK12(rho2SI, rho3SI);

    int p2 = p2mp1 + p1;

    double[] bNum = new double[p2 + 1];
    bNum[p1] = k12;
    bNum[p2] = k23;

    double[] aDen = new double[p2 + 1];
    aDen[0] = 1;
    aDen[p1] = -k12;
    aDen[p2] = -k23;
    aDen[p2 - p1] += k12 * k23;

    double[] q = CoefficientsUtils.serialize(bNum, aDen, INT + 1);

    return resistanceOneLayer.value(rho1SI) + 2.0 * ResistanceOneLayer.twoRhoByPI(rho1SI) * sum(q);
  }

  private double sum(@Nonnull double[] q) {
    return sum(q, (qn, b) -> qn *
        (1.0 / hypot(resistanceOneLayer.getElectrodeSystem().radiusMinus(), b)
            - 1.0 / hypot(resistanceOneLayer.getElectrodeSystem().radiusPlus(), b)));
  }

  private double sum(@Nonnull double[] q, @Nonnull BivariateFunction qAndB) {
    return sum(n -> qAndB.value(q[n], 4.0 * n * hStepSI));
  }

  private static double sum(@Nonnull IntToDoubleFunction operator) {
    return IntStream.rangeClosed(1, INT).unordered().parallel().mapToDouble(operator).sum();
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
