package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.pow;

/**
 * Calculates resistance R<sub>m-n</sub> (in Ohm) <b>normalized by rho<sub>1</sub</></b> between electrodes for <b>2-layer</b> model.
 */
final class NormalizedResistance2Layer extends AbstractResistanceLayer<Potential2Layer> implements DoubleBinaryOperator {
  @Nonnull
  private final NormalizedResistance1Layer resistance1Layer;

  NormalizedResistance2Layer(@Nonnull TetrapolarSystem electrodeSystem) {
    super(electrodeSystem, Potential2Layer::new);
    resistance1Layer = new NormalizedResistance1Layer(electrodeSystem);
  }

  @Override
  public double applyAsDouble(double k, @Nonnegative double h) {
    double result = 0.0;
    if (Double.compare(k, 0.0) != 0.0) {
      result += Layers.sum(n -> pow(k, n) * apply(r -> r.value(n, h)));
    }
    result *= (2.0 / Math.PI);
    return resistance1Layer.getAsDouble() + result;
  }
}
