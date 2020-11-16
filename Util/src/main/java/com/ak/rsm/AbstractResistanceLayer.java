package com.ak.rsm;

import java.util.function.DoubleFunction;
import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnull;

abstract class AbstractResistanceLayer<U extends AbstractPotentialLayer> {
  @Nonnull
  private final U uMns;
  @Nonnull
  private final U uPls;

  AbstractResistanceLayer(@Nonnull TetrapolarSystem electrodeSystem, DoubleFunction<U> potential) {
    uMns = potential.apply(electrodeSystem.radius(-1.0));
    uPls = potential.apply(electrodeSystem.radius(1.0));
  }

  final double apply(@Nonnull ToDoubleFunction<U> potentialValue) {
    return potentialValue.applyAsDouble(uMns) - potentialValue.applyAsDouble(uPls);
  }
}
