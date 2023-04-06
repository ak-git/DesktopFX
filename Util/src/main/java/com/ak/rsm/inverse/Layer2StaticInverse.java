package com.ak.rsm.inverse;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.ToDoubleBiFunction;

final class Layer2StaticInverse extends AbstractInverse implements ToDoubleBiFunction<TetrapolarSystem, double[]> {
  @Nonnull
  private final BiFunction<TetrapolarSystem, double[], RelativeMediumLayers> layersBiFunction;
  @Nonnull
  private final ToDoubleBiFunction<TetrapolarSystem, double[]> apparentPredicted;

  Layer2StaticInverse(@Nonnull Collection<TetrapolarSystem> systems) {
    super(systems);
    layersBiFunction = (s, kw) -> new Layer2RelativeMedium(kw[0], kw[1] * baseL() / s.lCC());
    apparentPredicted = (s, kw) -> Apparent2Rho.newApparentDivRho1(s.relativeSystem()).applyAsDouble(layersBiFunction.apply(s, kw));
  }

  @Override
  public double applyAsDouble(TetrapolarSystem s, double[] kw) {
    return apparentPredicted.applyAsDouble(s, kw);
  }

  @Nonnull
  BiFunction<TetrapolarSystem, double[], RelativeMediumLayers> layersBiFunction() {
    return layersBiFunction;
  }
}
