package com.ak.rsm.inverse;

import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

abstract sealed class AbstractInverseFunction<R extends Resistivity>
    implements InverseFunction, ToDoubleBiFunction<TetrapolarSystem, double[]>
    permits DynamicInverse, StaticInverse {
  @Nonnegative
  private final double baseL;
  @Nonnull
  private final List<TetrapolarSystem> systems;
  @Nonnull
  private final double[] measured;
  @Nonnull
  private final UnaryOperator<RelativeMediumLayers> toErrors;

  @ParametersAreNonnullByDefault
  AbstractInverseFunction(Collection<? extends R> r, ToDoubleFunction<? super R> toData, UnaryOperator<RelativeMediumLayers> toErrors) {
    baseL = Resistivity.getBaseL(r);
    systems = r.stream().map(Resistivity::system).toList();
    measured = r.stream().mapToDouble(toData).toArray();
    this.toErrors = toErrors;
  }

  @Nonnegative
  @Override
  public final double applyAsDouble(@Nonnull double[] kw) {
    double result = 0.0;
    for (int i = 0; i < measured.length; i++) {
      double v = measured[i] / applyAsDouble(systems.get(i), kw);
      if (v > 0) {
        result = StrictMath.hypot(result, StrictMath.log(v));
      }
      else {
        return Double.NaN;
      }
    }
    return result;
  }

  @Override
  @Nonnull
  public final RelativeMediumLayers apply(@Nonnull RelativeMediumLayers relativeMediumLayers) {
    return toErrors.apply(relativeMediumLayers);
  }

  @ParametersAreNonnullByDefault
  @Nonnull
  final RelativeMediumLayers layer2RelativeMedium(TetrapolarSystem s, double[] kw) {
    return new RelativeMediumLayers(kw[0], kw[1] * baseL / s.lCC());
  }
}
