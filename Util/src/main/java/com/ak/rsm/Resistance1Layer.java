package com.ak.rsm;

import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>single-layer</b> model.
 */
final class Resistance1Layer extends AbstractResistanceLayer<Potential1Layer> implements UnivariateFunction {
  Resistance1Layer(@Nonnull TetrapolarSystem electrodeSystem) {
    super(electrodeSystem, Potential1Layer::new);
  }

  /**
   * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm)
   *
   * @param rho specific resistance of <b>single-layer</b> in Ohm-m
   * @return resistance R<sub>m-n</sub> (in Ohm)
   */
  @Override
  public double value(double rho) {
    return applyAsDouble(u -> u.value(rho));
  }

  /**
   * Gets <b>apparent</b> specific resistance which is correspond to 1-layer model.
   *
   * @param rOhms in Ohms.
   * @return <b>apparent</b> specific resistance in Ohm-m.
   */
  double getApparent(@Nonnegative double rOhms) {
    return rOhms / value(1.0);
  }

  @Nonnull
  public static Medium inverse(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms) {
    double rho = IntStream.range(0, systems.length).mapToDouble(i -> new Resistance1Layer(systems[i]).getApparent(rOhms[i])).average().orElseThrow();
    return new Medium.Builder(systems, rOhms, s -> new Resistance1Layer(s).value(rho)).build(rho);
  }
}
