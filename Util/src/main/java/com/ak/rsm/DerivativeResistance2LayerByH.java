package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.TrivariateFunction;

/**
 * Calculates <b>first derivative</b> resistance R<sub>m-n</sub> (in Ohm) by <b>h</b> for 2-layer model.
 * <br/>
 * <b>dR<sub>h</sub></b>
 */
final class DerivativeResistance2LayerByH extends AbstractResistanceLayer<DerivatePotential2LayerByH> implements TrivariateFunction {
  DerivativeResistance2LayerByH(@Nonnull TetrapolarSystem electrodeSystem) {
    super(electrodeSystem, DerivatePotential2LayerByH::new);
  }

  @Override
  public double value(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double h) {
    return applyAsDouble(u -> u.value(rho1, rho2, h));
  }
}
