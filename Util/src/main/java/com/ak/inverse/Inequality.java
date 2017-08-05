package com.ak.inverse;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;

public final class Inequality implements DoubleBinaryOperator, DoubleSupplier {
  private static final DoubleBinaryOperator L2_NORM = StrictMath::hypot;
  private final DoubleBinaryOperator errorDefinition;
  private double errorNorm;

  private Inequality(DoubleBinaryOperator errorDefinition) {
    this.errorDefinition = errorDefinition;
  }

  public static Inequality logDifference() {
    return new Inequality((means, predicted) -> StrictMath.log(means) - StrictMath.log(predicted));
  }

  public static Inequality proportional() {
    return new Inequality((means, predicted) -> (means - predicted) / predicted);
  }

  static Inequality absolute() {
    return new Inequality((means, predicted) -> means - predicted);
  }

  @Override
  public double applyAsDouble(double means, double predicted) {
    errorNorm = L2_NORM.applyAsDouble(errorNorm, errorDefinition.applyAsDouble(means, predicted));
    return errorNorm;
  }

  @Override
  public double getAsDouble() {
    return errorNorm;
  }
}
