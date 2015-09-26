package com.ak.rsm;

import org.apache.commons.math3.analysis.TrivariateFunction;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>2-layer</b> model.
 */
public final class ResistanceTwoLayer implements TrivariateFunction, Cloneable {
  private final ResistanceOneLayer resistanceOneLayer;

  public ResistanceTwoLayer(TetrapolarSystem electrodeSystem) {
    resistanceOneLayer = new ResistanceOneLayer(electrodeSystem);
  }

  /**
   * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm)
   *
   * @param rho1SI specific resistance of <b>1st-layer</b> in Ohm-m
   * @param rho2SI specific resistance of <b>2nd-layer</b> in Ohm-m
   * @param hSI    height of <b>1-layer</b> in metres
   * @return resistance R<sub>m-n</sub> (in Ohm)
   */
  @Override
  public double value(double rho1SI, double rho2SI, double hSI) {
    double resistivity = resistanceOneLayer.value(rho1SI);

    if (Double.compare(rho1SI, rho2SI) == 0) {
      return resistivity;
    }
    else {
      double sum = 0.0;
      double k = (rho2SI - rho1SI) / (rho2SI + rho1SI);
      for (int n = 1; ; n++) {
        double b = 4.0 * n * hSI;
        double prev = sum;
        sum += StrictMath.pow(k, n) *
            (1.0 / StrictMath.hypot(resistanceOneLayer.getElectrodeSystem().radiusMinus(), b)
                - 1.0 / StrictMath.hypot(resistanceOneLayer.getElectrodeSystem().radiusPlus(), b));
        if (Double.compare(prev, sum) == 0) {
          break;
        }
      }
      return resistivity + 2.0 * ResistanceOneLayer.thoRhoByPI(rho1SI) * sum;
    }
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
