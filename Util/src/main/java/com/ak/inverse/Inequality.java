package com.ak.inverse;

import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleBiFunction;

import static java.lang.Math.abs;

public final class Inequality implements DoubleBinaryOperator, DoubleSupplier, ToDoubleBiFunction<double[], double[]> {
  private final DoubleBinaryOperator errorDefinition;
  private double errorNorm;

  private Inequality(DoubleBinaryOperator errorDefinition) {
    this.errorDefinition = Objects.requireNonNull(errorDefinition);
  }

  public static Inequality proportional() {
    return new Inequality((measured, predicted) -> abs((measured - predicted) / predicted));
  }

  public static Inequality absolute() {
    return new Inequality((measured, predicted) -> abs(measured - predicted));
  }

  @Override
  public double applyAsDouble(double measured, double predicted) {
    errorNorm = StrictMath.hypot(errorNorm, errorDefinition.applyAsDouble(measured, predicted));
    return errorNorm;
  }

  @Override
  public double applyAsDouble(double[] measured, double[] predicted) {
    for (var i = 0; i < measured.length; i++) {
      applyAsDouble(measured[i], predicted[i]);
    }
    return getAsDouble();
  }

  @Override
  public double getAsDouble() {
    return errorNorm;
  }
}
