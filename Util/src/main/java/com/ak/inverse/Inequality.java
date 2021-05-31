package com.ak.inverse;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleBiFunction;

import javax.annotation.Nonnull;

import static java.lang.Math.abs;

public final class Inequality implements DoubleBinaryOperator, DoubleSupplier, ToDoubleBiFunction<double[], double[]> {
  private static final DoubleBinaryOperator L2_NORM = StrictMath::hypot;
  @Nonnull
  private final DoubleBinaryOperator errorDefinition;
  private double errorNorm;

  private Inequality(@Nonnull DoubleBinaryOperator errorDefinition) {
    this.errorDefinition = errorDefinition;
  }

  public static Inequality proportional() {
    return new Inequality((measured, predicted) -> abs((measured - predicted) / predicted));
  }

  public static Inequality absolute() {
    return new Inequality((measured, predicted) -> abs(measured - predicted));
  }

  @Override
  public double applyAsDouble(double measured, double predicted) {
    errorNorm = L2_NORM.applyAsDouble(errorNorm, errorDefinition.applyAsDouble(measured, predicted));
    return errorNorm;
  }

  @Override
  public double applyAsDouble(@Nonnull double[] measured, @Nonnull double[] predicted) {
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
