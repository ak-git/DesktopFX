package com.ak.rsm;

import org.apache.commons.math3.analysis.TrivariateFunction;
import tec.uom.se.unit.Units;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

/**
 * Calculates <b>first derivative</b> resistance R<sub>m-n</sub> (in Ohm) by <b>Rho-2</b> normalized by Rho-2 / R for 2-layer model.
 * <br/>
 * <b>(dR<sub>Rho-2</sub>)(Rho-2 / R)</b>
 */
public final class DerivativeRbyRho2Normalized implements TrivariateFunction, Cloneable {
  /**
   * Calculates  <b>(dR<sub>Rho-2</sub>)(Rho-2 / R)</b>
   *
   * @param k12  (rho2 - rho2) / (rho2 + rho1)
   * @param sToL relation s / L
   * @param hToL relation h / L
   * @return <b>(dR<sub>Rho-2</sub>)(Rho-2 / R)</b>
   */
  @Override
  public double value(double k12, double sToL, double hToL) {
    TetrapolarSystem electrodes = new TetrapolarSystem(sToL, 1.0, Units.METRE);
    ResistanceTwoLayer rTwoLayer = new ResistanceTwoLayer(electrodes);
    double denominator = 1.0 / electrodes.radiusMinus() - 1.0 / electrodes.radiusPlus() + 2.0 * rTwoLayer.sum(k12, hToL);
    return nominator(k12, hToL, electrodes) / denominator;
  }

  private static double nominator(double k12, double hSI, TetrapolarSystem electrodes) {
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
