package com.ak.rsm;

import java.util.function.DoubleFunction;
import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

abstract class AbstractResistanceLayer<U extends AbstractPotentialLayer> {
  @Nonnull
  private final U uMns;
  @Nonnull
  private final U uPls;

  @ParametersAreNonnullByDefault
  AbstractResistanceLayer(TetrapolarSystem system, DoubleFunction<U> potential) {
    uMns = potential.apply(radius(system, -1.0));
    uPls = potential.apply(radius(system, 1.0));
  }

  final double apply(@Nonnull ToDoubleFunction<U> potentialValue) {
    return potentialValue.applyAsDouble(uMns) - potentialValue.applyAsDouble(uPls);
  }

  private static double radius(@Nonnull TetrapolarSystem system, double sign) {
    return system.getL() * system.factor(sign) / 2.0;
  }
}
