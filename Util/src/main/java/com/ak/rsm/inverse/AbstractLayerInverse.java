package com.ak.rsm.inverse;

import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.function.ToDoubleBiFunction;

abstract class AbstractLayerInverse extends AbstractInverse implements ToDoubleBiFunction<TetrapolarSystem, double[]> {
  @Nonnull
  private final ToDoubleBiFunction<TetrapolarSystem, double[]> predicted;

  @ParametersAreNonnullByDefault
  AbstractLayerInverse(Collection<TetrapolarSystem> systems, Supplier<ToDoubleBiFunction<TetrapolarSystem, double[]>> toPredicted) {
    super(systems);
    predicted = toPredicted.get();
  }

  @Override
  @ParametersAreNonnullByDefault
  public final double applyAsDouble(TetrapolarSystem s, double[] kw) {
    return predicted.applyAsDouble(s, kw);
  }
}
