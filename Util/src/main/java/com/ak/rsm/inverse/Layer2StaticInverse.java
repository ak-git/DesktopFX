package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.ToDoubleBiFunction;

import javax.annotation.Nonnull;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.TetrapolarSystem;

final class Layer2StaticInverse extends AbstractInverse implements ToDoubleBiFunction<TetrapolarSystem, double[]> {
  @Nonnull
  private final BiFunction<TetrapolarSystem, double[], RelativeMediumLayers> layersBiFunction;
  @Nonnull
  private final ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted;

  Layer2StaticInverse(@Nonnull Collection<TetrapolarSystem> systems) {
    super(systems);
    layersBiFunction = (s, kw) -> new Layer2RelativeMedium(kw[0], kw[1] * baseL() / s.lCC());
    logApparentPredicted = (s, kw) ->
        Apparent2Rho.newLog1pApparent2Rho(s.relativeSystem()).applyAsDouble(layersBiFunction.apply(s, kw));
  }

  @Override
  public double applyAsDouble(TetrapolarSystem s, double[] kw) {
    return logApparentPredicted.applyAsDouble(s, kw);
  }

  @Nonnull
  BiFunction<TetrapolarSystem, double[], RelativeMediumLayers> layersBiFunction() {
    return layersBiFunction;
  }
}
