package com.ak.rsm.resistance;

import java.util.function.DoubleSupplier;

import javax.annotation.Nonnull;

import com.ak.rsm.potential.Potential1Layer;
import com.ak.rsm.system.TetrapolarSystem;

/**
 * Calculates ohms R<sub>m-n</sub> (in Ohm) <b>normalized by rho<sub>1</sub</></b> between electrodes for <b>single-layer</b> model.
 */
final class NormalizedResistance1Layer extends AbstractResistanceLayer<Potential1Layer> implements DoubleSupplier {
  NormalizedResistance1Layer(@Nonnull TetrapolarSystem electrodeSystem) {
    super(electrodeSystem, Potential1Layer::new);
  }

  @Override
  public double getAsDouble() {
    return apply(Potential1Layer::value) / Math.PI;
  }
}
