package com.ak.rsm2;

import java.util.function.DoubleUnaryOperator;

enum Sign implements DoubleUnaryOperator {
  PLUS(1), MINUS(-1);

  private final int signCoeff;

  Sign(int signCoeff) {
    this.signCoeff = signCoeff;
  }

  @Override
  public final double applyAsDouble(double operand) {
    return signCoeff * operand;
  }
}
