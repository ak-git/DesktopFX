package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;

abstract class AbstractInverseFunction<R extends Resistivity> extends AbstractInverse
    implements ToDoubleFunction<double[]>, UnaryOperator<double[]> {
  @Nonnull
  private final BiFunction<TetrapolarSystem, double[], RelativeMediumLayers> layersBiFunction;
  @Nonnull
  private final ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted;
  @Nonnull
  private final double[] subLog;
  @Nonnull
  private final UnaryOperator<double[]> subtract;

  @ParametersAreNonnullByDefault
  AbstractInverseFunction(Collection<? extends R> r, ToDoubleFunction<? super R> function, UnaryOperator<double[]> subtract) {
    super(r.stream().map(Resistivity::system).toList());
    this.subtract = subtract;
    subLog = subtract.apply(r.stream().mapToDouble(function).toArray());
    layersBiFunction = (s1, kw1) -> new Layer2RelativeMedium(kw1[0], kw1[1] * baseL() / s1.lCC());
    logApparentPredicted = (s, kw) -> Apparent2Rho.newLog1pApparent2Rho(s.relativeSystem()).applyAsDouble(layersBiFunction.apply(s, kw));
  }

  @Nonnegative
  @Override
  public final double applyAsDouble(@Nonnull double[] kw) {
    return Inequality.absolute().applyAsDouble(subLog, subtract.apply(apply(kw)));
  }

  @Nonnull
  final UnaryOperator<double[]> subtract() {
    return subtract;
  }

  @Nonnull
  final BiFunction<TetrapolarSystem, double[], RelativeMediumLayers> layersBiFunction() {
    return layersBiFunction;
  }

  @Nonnull
  final ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted() {
    return logApparentPredicted;
  }
}
