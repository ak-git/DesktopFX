package com.ak.rsm.resistance;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.system.TetrapolarSystem;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * Calculates <b>full</b> ohms R<sub>m-n</sub> (in Ohm) between electrodes for <b>single-layer</b> model.
 */
record Resistance1Layer(@Nonnull NormalizedResistance1Layer resistance) implements UnivariateFunction {
  Resistance1Layer(@Nonnull TetrapolarSystem electrodeSystem) {
    this(new NormalizedResistance1Layer(electrodeSystem));
  }

  /**
   * Calculates <b>full</b> ohms R<sub>m-n</sub> (in Ohm)
   *
   * @param rho specific ohms of <b>single-layer</b> in Ohm-m
   * @return ohms R<sub>m-n</sub> (in Ohm)
   */
  @Override
  public double value(@Nonnegative double rho) {
    return rho * resistance.getAsDouble();
  }
}
