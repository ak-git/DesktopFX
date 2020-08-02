package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>single-layer</b> model.
 */
final class Resistance1Layer implements UnivariateFunction {
  @Nonnull
  private final NormalizedResistance1Layer resistance;

  Resistance1Layer(@Nonnull TetrapolarSystem electrodeSystem) {
    resistance = new NormalizedResistance1Layer(electrodeSystem);
  }

  /**
   * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm)
   *
   * @param rho specific resistance of <b>single-layer</b> in Ohm-m
   * @return resistance R<sub>m-n</sub> (in Ohm)
   */
  @Override
  public double value(@Nonnegative double rho) {
    return rho * resistance.getAsDouble();
  }
}
