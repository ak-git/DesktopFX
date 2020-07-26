package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.TrivariateFunction;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>2-layer</b> model.
 */
final class Resistance2Layer implements TrivariateFunction {
  @Nonnull
  private final NormalizedResistance2Layer resistance2Layer;

  Resistance2Layer(@Nonnull TetrapolarSystem electrodeSystem) {
    resistance2Layer = new NormalizedResistance2Layer(electrodeSystem);
  }

  @Override
  public double value(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double h) {
    return rho1 * resistance2Layer.applyAsDouble(Layers.getK12(rho1, rho2), h);
  }
}
