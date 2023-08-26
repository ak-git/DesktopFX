package com.ak.rsm.inverse;

import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

abstract sealed class AbstractInverseFunction<R extends Resistivity>
    implements InverseFunction, ToDoubleBiFunction<TetrapolarSystem, double[]>
    permits DynamicInverse, StaticInverse {
  private record Experiment(@Nonnull TetrapolarSystem system, double measured) {
  }

  @Nonnegative
  private final double baseL;
  @Nonnull
  private final Collection<Experiment> experiments;
  @Nonnull
  private final UnaryOperator<RelativeMediumLayers> toErrors;

  @ParametersAreNonnullByDefault
  AbstractInverseFunction(Collection<? extends R> r, ToDoubleFunction<? super R> toData, UnaryOperator<RelativeMediumLayers> toErrors) {
    baseL = Resistivity.getBaseL(r);
    experiments = r.stream().map(res -> new Experiment(res.system(), toData.applyAsDouble(res))).toList();
    this.toErrors = toErrors;
  }

  @Nonnegative
  @Override
  public final double applyAsDouble(@Nonnull double[] kw) {
    return experiments.stream()
        .mapToDouble(e -> e.measured / applyAsDouble(e.system, kw)).map(StrictMath::log)
        .filter(Double::isFinite).reduce(Math::hypot).orElse(Double.POSITIVE_INFINITY);
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
