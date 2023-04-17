package com.ak.rsm.inverse;

import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

abstract sealed class AbstractInverseFunction<R extends Resistivity> extends AbstractInverse
    implements ToDoubleFunction<double[]>, ToDoubleBiFunction<TetrapolarSystem, double[]>
    permits DynamicInverse, StaticInverse {
  private record Experiment(@Nonnull TetrapolarSystem system, double measured) {
  }

  @Nonnull
  private final Collection<Experiment> experiments;

  @ParametersAreNonnullByDefault
  AbstractInverseFunction(Collection<? extends R> r, ToDoubleFunction<? super R> toData) {
    super(r.stream().map(Resistivity::system).toList());
    experiments = r.stream().map(res -> new Experiment(res.system(), toData.applyAsDouble(res))).toList();
  }

  @Nonnegative
  @Override
  public final double applyAsDouble(@Nonnull double[] kw) {
    return experiments.stream()
        .map(e -> e.measured / applyAsDouble(e.system, kw)).mapToDouble(StrictMath::log)
        .filter(Double::isFinite).reduce(Math::hypot).orElse(Double.POSITIVE_INFINITY);
  }
}
