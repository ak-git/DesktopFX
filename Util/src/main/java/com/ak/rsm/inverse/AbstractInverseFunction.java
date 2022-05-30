package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;
import org.apache.commons.math3.complex.Complex;

abstract class AbstractInverseFunction<R extends Resistivity> extends AbstractInverse implements ToDoubleFunction<double[]> {
  @Nonnull
  private final Complex[] subLog;
  @Nonnull
  private final UnaryOperator<double[]> subtract;
  @Nonnull
  final BiFunction<TetrapolarSystem, double[], Complex> predicted;

  @ParametersAreNonnullByDefault
  AbstractInverseFunction(Collection<? extends R> r, Function<? super R, Complex> toData, UnaryOperator<double[]> subtract,
                          Function<Collection<TetrapolarSystem>, BiFunction<TetrapolarSystem, double[], Complex>> toPredicted) {
    super(r.stream().map(Resistivity::system).toList());
    this.subtract = subtract;
    subLog = getLog(
        f -> r.stream().mapToDouble(
            value -> f.applyAsDouble(toData.apply(value))
        ).toArray());
    predicted = toPredicted.apply(systems());
  }

  @Nonnegative
  @Override
  public final double applyAsDouble(@Nonnull double[] kw) {
    Complex[] subLogPredicted = getLog(
        f -> systems().stream().mapToDouble(
            s -> f.applyAsDouble(predicted.apply(s, kw))
        ).toArray()
    );
    return IntStream.range(0, subLog.length).mapToDouble(i -> subLog[i].subtract(subLogPredicted[i]).abs())
        .reduce(Math::hypot).orElseThrow();
  }

  @Nonnull
  final UnaryOperator<double[]> subtract() {
    return subtract;
  }

  @Nonnull
  private Complex[] getLog(@Nonnull Function<ToDoubleFunction<Complex>, double[]> function) {
    return Stream.<ToDoubleFunction<Complex>>of(Complex::getReal, Complex::getImaginary)
        .map(function)
        .map(subtract)
        .map(doubles -> Arrays.stream(doubles).mapToObj(Complex::valueOf).toArray(Complex[]::new))
        .reduce((complexes1, complexes2) -> {
          Complex[] result = new Complex[Math.max(complexes1.length, complexes2.length)];
          for (int i = 0; i < result.length; i++) {
            result[i] = new Complex(complexes1[i].getReal(), complexes2[i].getReal());
          }
          return result;
        }).orElseThrow();
  }
}
