package com.ak.rsm;

import java.util.function.DoubleSupplier;

import javax.annotation.Nonnull;

/**
 * Calculates resistance R<sub>m-n</sub> (in Ohm) <b>normalized by rho<sub>1</sub</></b> between electrodes for <b>single-layer</b> model.
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
