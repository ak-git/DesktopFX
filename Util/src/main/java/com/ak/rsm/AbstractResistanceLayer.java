package com.ak.rsm;

import java.util.function.DoubleFunction;
import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnull;

abstract class AbstractResistanceLayer<U extends AbstractPotentialLayer> implements ToDoubleFunction<ToDoubleFunction<U>> {
  private final U uMns;
  private final U uPls;

  AbstractResistanceLayer(@Nonnull TetrapolarSystem electrodeSystem, DoubleFunction<U> potential) {
    uMns = potential.apply(electrodeSystem.radiusMns());
    uPls = potential.apply(electrodeSystem.radiusPls());
  }

  @Override
  public final double applyAsDouble(ToDoubleFunction<U> potentialValue) {
    return 2.0 * (potentialValue.applyAsDouble(uMns) - potentialValue.applyAsDouble(uPls));
  }
}
