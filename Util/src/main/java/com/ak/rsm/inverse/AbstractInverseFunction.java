package com.ak.rsm.inverse;

import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

abstract class AbstractInverseFunction<R extends Resistivity>
    implements InverseFunction, ToDoubleBiFunction<TetrapolarSystem, double[]> {
  @Nonnegative
  private final double baseL;
  private final List<TetrapolarSystem> systems;
  private final double[] measured;
  private final UnaryOperator<RelativeMediumLayers> toErrors;

  AbstractInverseFunction(Collection<? extends R> r, ToDoubleFunction<? super R> toData, UnaryOperator<RelativeMediumLayers> toErrors) {
    baseL = Resistivity.getBaseL(r);
    systems = r.stream().map(Resistivity::system).toList();
    measured = r.stream().mapToDouble(Objects.requireNonNull(toData)).toArray();
    this.toErrors = Objects.requireNonNull(toErrors);
  }

  @Nonnegative
  @Override
  public final double applyAsDouble(double[] kw) {
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
  public final RelativeMediumLayers apply(RelativeMediumLayers relativeMediumLayers) {
    return toErrors.apply(relativeMediumLayers);
  }

  final RelativeMediumLayers layer2RelativeMedium(TetrapolarSystem s, double[] kw) {
    return new RelativeMediumLayers(kw[0], kw[1] * baseL / s.lCC());
  }
}
