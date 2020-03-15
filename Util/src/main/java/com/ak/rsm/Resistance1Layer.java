package com.ak.rsm;

import java.util.function.IntToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Strings;
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
  public double value(@Nonnegative double rho) {
    return (rho / Math.PI) * apply(Potential1Layer::value);
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
  @ParametersAreNonnullByDefault
  public static Medium inverseStatic(TetrapolarSystem[] systems, double[] rOhms) {
    IntToDoubleFunction apparent = i -> new Resistance1Layer(systems[i]).getApparent(rOhms[i]);
    double rho = IntStream.range(0, systems.length).mapToDouble(apparent).average().orElseThrow();
    Medium medium = new Medium.Builder(systems, rOhms, s -> new Resistance1Layer(s).value(rho)).build(rho);
    Logger.getLogger(Resistance1Layer.class.getName()).info(
        () ->
            String.join(Strings.NEW_LINE,
                IntStream.range(0, systems.length)
                    .mapToObj(i -> String.format("%n%s, %s", systems[i], Strings.rho(apparent.applyAsDouble(i))))
                    .collect(Collectors.joining()),
                medium.toString()
            )
    );
    return medium;
  }
}
