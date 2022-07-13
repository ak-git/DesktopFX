package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.system.TetrapolarSystem;
import org.apache.commons.math3.complex.Complex;

abstract class AbstractLayerInverse extends AbstractInverse implements BiFunction<TetrapolarSystem, double[], Complex> {
  @Nonnull
  private final BiFunction<TetrapolarSystem, double[], Complex> predicted;

  @ParametersAreNonnullByDefault
  AbstractLayerInverse(Collection<TetrapolarSystem> systems, Supplier<BiFunction<TetrapolarSystem, double[], Complex>> toPredicted) {
    super(systems);
    predicted = toPredicted.get();
  }

  @Override
  public final Complex apply(TetrapolarSystem s, double[] kw) {
    return predicted.apply(s, kw);
  }
}
