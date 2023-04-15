package com.ak.rsm.inverse;

import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

final class DynamicInverse extends AbstractInverseFunction<DerivativeResistivity> {
  @ParametersAreNonnullByDefault
  private DynamicInverse(Collection<? extends DerivativeResistivity> r,
                         Function<Collection<TetrapolarSystem>, ToDoubleBiFunction<TetrapolarSystem, double[]>> toPredicted) {
    super(r, d -> d.resistivity() / d.derivativeResistivity(), toPredicted);
  }

  static ToDoubleFunction<double[]> of(@Nonnull Collection<? extends DerivativeResistivity> r) {
    return new DynamicInverse(r, systems -> new Layer2DynamicInverse(systems, dH(r)));
  }

  static ToDoubleFunction<double[]> of(@Nonnull Collection<? extends DerivativeResistivity> r, @Nonnegative double hStep) {
    return new DynamicInverse(r, systems -> new Layer3DynamicInverse(systems, hStep, dH(r)));
  }

  private static double dH(@Nonnull Collection<? extends DerivativeResistivity> r) {
    return r.stream().mapToDouble(DerivativeResistivity::dh)
        .reduce((left, right) -> Double.compare(left, right) == 0 ? left : Double.NaN).orElse(Double.NaN);
  }
}
