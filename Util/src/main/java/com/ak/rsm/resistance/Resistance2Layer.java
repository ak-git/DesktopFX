package com.ak.rsm.resistance;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import org.apache.commons.math3.analysis.TrivariateFunction;

/**
 * Calculates <b>full</b> ohms R<sub>m-n</sub> (in Ohm) between electrodes for <b>2-layer</b> model.
 */
public final class Resistance2Layer implements TrivariateFunction {
  @Nonnull
  private final NormalizedResistance2Layer resistance;

  public Resistance2Layer(@Nonnull TetrapolarSystem electrodeSystem) {
    resistance = new NormalizedResistance2Layer(electrodeSystem);
  }

  @Override
  public double value(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double h) {
    return rho1 * resistance.applyAsDouble(Layers.getK12(rho1, rho2), h);
  }
}
