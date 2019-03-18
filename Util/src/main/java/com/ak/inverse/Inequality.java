package com.ak.inverse;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleBiFunction;

import javax.annotation.Nonnull;

import static java.lang.Math.abs;

public final class Inequality implements DoubleBinaryOperator, DoubleSupplier, ToDoubleBiFunction<double[], double[]> {
  private static final DoubleBinaryOperator L2_NORM = StrictMath::hypot;
  private static final DoubleBinaryOperator LOG1P = (measured, predicted) -> abs(StrictMath.log1p(abs(measured)) - StrictMath.log1p(abs(predicted)));
  @Nonnull
  private final DoubleBinaryOperator errorDefinition;
  private double errorNorm;

  private Inequality(@Nonnull DoubleBinaryOperator errorDefinition) {
    this.errorDefinition = errorDefinition;
  }

  public static Inequality expAndLogDifference() {
    return new Inequality((measured, predicted) -> {
      if ((measured < 0 && predicted > 0) || (measured > 0 && predicted < 0)) {
        return StrictMath.expm1(abs(measured)) + StrictMath.expm1(abs(predicted));
      }
      else {
        return LOG1P.applyAsDouble(measured, predicted);
      }
    });
  }

  public static Inequality logDifference() {
    return new Inequality((measured, predicted) -> abs(StrictMath.log(abs(measured)) - StrictMath.log(abs(predicted))));
  }

  public static Inequality log1pDifference() {
    return new Inequality(LOG1P);
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
  public double applyAsDouble(double[] measured, double[] predicted) {
    for (int i = 0; i < measured.length; i++) {
      applyAsDouble(measured[i], predicted[i]);
    }
    return getAsDouble();
  }

  @Override
  public double getAsDouble() {
    return errorNorm;
  }
}
