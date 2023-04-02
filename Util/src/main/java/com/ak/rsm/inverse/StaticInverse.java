package com.ak.rsm.inverse;

import com.ak.rsm.resistance.Resistivity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.UnaryOperator;

final class StaticInverse extends AbstractInverseFunction<Resistivity> {
  @ParametersAreNonnullByDefault
  StaticInverse(Collection<? extends Resistivity> r, UnaryOperator<double[]> subtract) {
    super(r, Resistivity::resistivity, subtract, Layer2StaticInverse::new);
  }
}
