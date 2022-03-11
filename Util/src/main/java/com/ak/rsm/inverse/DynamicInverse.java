package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.system.TetrapolarSystem;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.log;

final class DynamicInverse extends AbstractInverseFunction<DerivativeResistivity> {
  @ParametersAreNonnullByDefault
  private DynamicInverse(Collection<? extends DerivativeResistivity> r,
                         Function<Collection<TetrapolarSystem>, ToDoubleBiFunction<TetrapolarSystem, double[]>> toPredicted) {
    super(r, d -> log(d.resistivity()) - log(abs(d.derivativeResistivity())), UnaryOperator.identity(), toPredicted);
  }

  static ToDoubleFunction<double[]> of(@Nonnull Collection<? extends DerivativeResistivity> r) {
    return new DynamicInverse(r, Layer2DynamicInverse::new);
  }

  static ToDoubleFunction<double[]> of(@Nonnull Collection<? extends DerivativeResistivity> r, @Nonnegative double hStep) {
    return new DynamicInverse(r, systems -> new Layer3DynamicInverse(systems, hStep));
  }
}
