package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.resistance.Resistivity;

import static java.lang.StrictMath.log;

final class StaticInverse extends AbstractInverseFunction<Resistivity> {
  @ParametersAreNonnullByDefault
  StaticInverse(Collection<? extends Resistivity> r, UnaryOperator<double[]> subtract) {
    super(r, d -> log(d.resistivity()), subtract);
  }

  @Override
  public double[] apply(@Nonnull double[] kw) {
    return systems().stream().mapToDouble(s -> logApparentPredicted().applyAsDouble(s, kw)).toArray();
  }
}
