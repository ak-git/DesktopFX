package com.ak.rsm.inverse;

import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

abstract class AbstractInverseFunction<R extends Resistivity> extends AbstractInverse implements ToDoubleFunction<double[]> {
  @Nonnull
  private final double[] measured;
  @Nonnull
  private final ToDoubleBiFunction<TetrapolarSystem, double[]> predicted;

  @ParametersAreNonnullByDefault
  AbstractInverseFunction(Collection<? extends R> r, ToDoubleFunction<? super R> toData,
                          Function<Collection<TetrapolarSystem>, ToDoubleBiFunction<TetrapolarSystem, double[]>> toPredicted) {
    super(r.stream().map(Resistivity::system).toList());
    measured = r.stream().mapToDouble(toData).toArray();
    predicted = toPredicted.apply(systems());
  }

  @Nonnegative
  @Override
  public final double applyAsDouble(@Nonnull double[] kw) {
    double[] model = systems().stream().mapToDouble(s -> predicted.applyAsDouble(s, kw)).toArray();
    double[] err = new double[Math.max(measured.length, model.length)];
    for (int i = 0; i < err.length; i++) {
      err[i] = StrictMath.log(measured[i] / model[i]);
    }
    return Arrays.stream(err).filter(Double::isFinite).reduce(Math::hypot).orElse(Double.POSITIVE_INFINITY);
  }
}
