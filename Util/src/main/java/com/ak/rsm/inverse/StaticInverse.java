package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.resistance.Resistivity;

final class StaticInverse extends AbstractInverseFunction {
  @Nonnull
  private final UnaryOperator<double[]> subtract;

  @ParametersAreNonnullByDefault
  StaticInverse(Collection<? extends Resistivity> r, UnaryOperator<double[]> subtract) {
    super(r, subtract.apply(r.stream().map(Resistivity::resistivity).mapToDouble(StrictMath::log).toArray()));
    this.subtract = subtract;
  }

  @Override
  public double[] apply(@Nonnull double[] kw) {
    return subtract.apply(
        systems().stream().mapToDouble(s -> logApparentPredicted().applyAsDouble(s, kw)).toArray()
    );
  }

  @Nonnull
  UnaryOperator<double[]> subtract() {
    return subtract;
  }
}
