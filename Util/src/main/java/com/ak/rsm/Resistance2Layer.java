package com.ak.rsm;

import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.TrivariateFunction;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>2-layer</b> model.
 */
final class Resistance2Layer implements TrivariateFunction {
  @Nonnull
  private final NormalizedResistance2Layer resistance;

  Resistance2Layer(@Nonnull TetrapolarSystem electrodeSystem) {
    resistance = new NormalizedResistance2Layer(electrodeSystem);
  }

  @Override
  public double value(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double h) {
    return rho1 * resistance.applyAsDouble(Layers.getK12(rho1, rho2), h);
  }

  @Nonnull
  static ToDoubleFunction<InexactTetrapolarSystem> layer2(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double h) {
    return system -> new Resistance2Layer(system.toExact()).value(rho1, rho2, h);
  }
}
