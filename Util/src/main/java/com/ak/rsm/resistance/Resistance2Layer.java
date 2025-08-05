package com.ak.rsm.resistance;

import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import org.apache.commons.math3.analysis.TrivariateFunction;

/**
 * Calculates <b>full</b> ohms R<sub>m-n</sub> (in Ohm) between electrodes for <b>2-layer</b> model.
 */
record Resistance2Layer(NormalizedResistance2Layer resistance) implements TrivariateFunction {
  Resistance2Layer(TetrapolarSystem electrodeSystem) {
    this(new NormalizedResistance2Layer(electrodeSystem));
  }

  @Override
  public double value(double rho1, double rho2, double h) {
    return rho1 * resistance.applyAsDouble(Layers.getK12(rho1, rho2), h);
  }
}
