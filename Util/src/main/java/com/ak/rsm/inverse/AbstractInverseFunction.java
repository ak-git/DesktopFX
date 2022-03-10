package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;

abstract class AbstractInverseFunction<R extends Resistivity> extends AbstractInverse
    implements ToDoubleFunction<double[]>, ToDoubleBiFunction<TetrapolarSystem, double[]> {
  @Nonnull
  private final double[] subLog;
  @Nonnull
  private final UnaryOperator<double[]> subtract;

  @ParametersAreNonnullByDefault
  AbstractInverseFunction(Collection<? extends R> r, ToDoubleFunction<? super R> function, UnaryOperator<double[]> subtract) {
    super(r.stream().map(Resistivity::system).toList());
    this.subtract = subtract;
    subLog = subtract.apply(r.stream().mapToDouble(function).toArray());
  }

  @Nonnegative
  @Override
  public final double applyAsDouble(@Nonnull double[] kw) {
    return Inequality.absolute().applyAsDouble(subLog,
        subtract.apply(systems().stream().mapToDouble(s -> applyAsDouble(s, kw)).toArray())
    );
  }

  @Nonnull
  final UnaryOperator<double[]> subtract() {
    return subtract;
  }
}
