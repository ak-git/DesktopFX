package com.ak.rsm.resistance;

import com.ak.rsm.potential.AbstractPotentialLayer;
import com.ak.rsm.system.TetrapolarSystem;

import java.util.function.DoubleFunction;
import java.util.function.ToDoubleFunction;

abstract class AbstractResistanceLayer<U extends AbstractPotentialLayer> {
  private final U uMns;
  private final U uPls;

  AbstractResistanceLayer(TetrapolarSystem electrodeSystem, DoubleFunction<U> potential) {
    uMns = potential.apply(electrodeSystem.factor(-1.0));
    uPls = potential.apply(electrodeSystem.factor(1.0));
  }

  final double apply(ToDoubleFunction<U> potentialValue) {
    return potentialValue.applyAsDouble(uMns) - potentialValue.applyAsDouble(uPls);
  }
}
