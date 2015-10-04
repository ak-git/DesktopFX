package com.ak.rsm;

import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>single-layer</b> model.
 */
final class ResistanceOneLayer implements UnivariateFunction, Cloneable {
  private final TetrapolarSystem electrodeSystem;

  ResistanceOneLayer(TetrapolarSystem electrodeSystem) {
    this.electrodeSystem = electrodeSystem;
  }

  /**
   * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm)
   *
   * @param rhoSI specific resistance of <b>single-layer</b> in Ohm-m
   * @return resistance R<sub>m-n</sub> (in Ohm)
   */
  @Override
  public double value(double rhoSI) {
    return thoRhoByPI(rhoSI) * (1.0 / electrodeSystem.radiusMinus() - 1.0 / electrodeSystem.radiusPlus());
  }

  public TetrapolarSystem getElectrodeSystem() {
    return electrodeSystem;
  }

  static double thoRhoByPI(double rhoSI) {
    return 2.0 * rhoSI / Math.PI;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
