package com.ak.rsm;

import org.apache.commons.math3.analysis.UnivariateFunction;
import tec.uom.se.unit.Units;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

/**
 * Calculates <b>first derivative</b> resistance R<sub>m-n</sub> (in Ohm) by <b>Rho-2</b> normalized by Rho-2 / R for 2-layer model.
 * <br/>
 * <b>(dR<sub>Rho-2</sub>)(Rho-2 / R)</b>
 */
public final class DerivativeRbyRho2Normalized implements UnivariateFunction, Cloneable {
  private final TetrapolarSystem electrodes;
  private final double k12;

  /**
   * Construct Derivative R by Rho2 Normalized by Rho2 / R
   *
   * @param k12  (rho2 - rho2) / (rho2 + rho1)
   * @param sToL relation s / L
   */
  public DerivativeRbyRho2Normalized(double k12, double sToL) {
    this.k12 = k12;
    electrodes = new TetrapolarSystem(sToL, 1.0, Units.METRE);
  }

  /**
   * Calculates  <b>(dR<sub>Rho-2</sub>)(Rho-2 / R)</b>
   *
   * @param hToL relation h / L
   * @return <b>(dR<sub>Rho-2</sub>)(Rho-2 / R)</b>
   */
  @Override
  public double value(double hToL) {
    ResistanceTwoLayer rTwoLayer = new ResistanceTwoLayer(electrodes);
    double denominator = 1.0 / electrodes.radiusMinus() - 1.0 / electrodes.radiusPlus() + 2.0 * rTwoLayer.sum(k12, hToL);
    return nominator(hToL) / denominator;
  }

  private double nominator(double hSI) {
    double sum = 0.0;
    for (int n = 1; ; n++) {
      double b = 4.0 * n * hSI;
      double prev = sum;
      sum += n * pow(k12, n - 1) * (1.0 / hypot(electrodes.radiusMinus(), b) - 1.0 / hypot(electrodes.radiusPlus(), b));
      if (Double.compare(prev, sum) == 0) {
        break;
      }
    }
    return (1.0 - pow(k12, 2.0)) * sum;
  }


  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
