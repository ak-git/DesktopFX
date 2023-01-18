package com.ak.rsm.resistance;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.potential.Potential2Layer;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;

import static java.lang.StrictMath.pow;

/**
 * Calculates ohms R<sub>m-n</sub> (in Ohm) <b>normalized by rho<sub>1</sub</></b> between electrodes for <b>2-layer</b> model.
 */
public final class NormalizedResistance2Layer extends AbstractResistanceLayer<Potential2Layer> implements DoubleBinaryOperator {
  @Nonnull
  private final NormalizedResistance1Layer resistance1Layer;

  public NormalizedResistance2Layer(@Nonnull TetrapolarSystem electrodeSystem) {
    super(electrodeSystem, Potential2Layer::new);
    resistance1Layer = new NormalizedResistance1Layer(electrodeSystem);
  }

  @Override
  public double applyAsDouble(double k, @Nonnegative double h) {
    var result = 0.0;
    if (Double.compare(k, 0.0) != 0.0) {
      result += Layers.sum(n -> pow(k, n) * apply(r -> r.value(n, h))) * 2.0 / Math.PI;
    }
    return resistance1Layer.getAsDouble() + result;
  }
}
