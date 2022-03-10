package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.ToDoubleBiFunction;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;

import static java.lang.StrictMath.log;

final class StaticInverse extends AbstractInverseFunction<Resistivity> {
  @Nonnull
  private final BiFunction<TetrapolarSystem, double[], RelativeMediumLayers> layersBiFunction;
  @Nonnull
  private final ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted;

  @ParametersAreNonnullByDefault
  StaticInverse(Collection<? extends Resistivity> r, UnaryOperator<double[]> subtract) {
    super(r, d -> log(d.resistivity()), subtract);
    layersBiFunction = (s, kw) -> new Layer2RelativeMedium(kw[0], kw[1] * baseL() / s.lCC());
    logApparentPredicted = (s, kw) ->
        Apparent2Rho.newLog1pApparent2Rho(s.relativeSystem()).applyAsDouble(layersBiFunction.apply(s, kw));
  }

  @Override
  @ParametersAreNonnullByDefault
  public double applyAsDouble(TetrapolarSystem s, double[] kw) {
    return logApparentPredicted.applyAsDouble(s, kw);
  }

  @Nonnull
  BiFunction<TetrapolarSystem, double[], RelativeMediumLayers> layersBiFunction() {
    return layersBiFunction;
  }
}
