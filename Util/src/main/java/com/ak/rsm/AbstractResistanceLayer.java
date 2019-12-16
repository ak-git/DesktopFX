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
    uMns = potential.apply(electrodeSystem.radiusMns());
    uPls = potential.apply(electrodeSystem.radiusPls());
  }

  final double apply(@Nonnull ToDoubleFunction<U> potentialValue) {
    return potentialValue.applyAsDouble(uMns) - potentialValue.applyAsDouble(uPls);
  }
}
