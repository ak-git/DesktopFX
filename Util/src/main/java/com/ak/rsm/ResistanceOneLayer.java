package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>single-layer</b> model.
 */
final class ResistanceOneLayer implements UnivariateFunction {
  @Nonnull
  private final TetrapolarSystem electrodeSystem;

  ResistanceOneLayer(@Nonnull TetrapolarSystem electrodeSystem) {
    this.electrodeSystem = electrodeSystem;
  }

  /**
   * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm)
   *
   * @param rhoSI specific resistance of <b>single-layer</b> in Ohm-m
   * @return resistance R<sub>m-n</sub> (in Ohm)
   */
  @Override
  public double value(@Nonnegative double rhoSI) {
    return twoRhoByPI(rhoSI) * (1.0 / electrodeSystem.radiusMinus() - 1.0 / electrodeSystem.radiusPlus());
  }

  TetrapolarSystem getElectrodeSystem() {
    return electrodeSystem;
  }

  @Nonnegative
  static double twoRhoByPI(@Nonnegative double rhoSI) {
    return 2.0 * rhoSI / Math.PI;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
