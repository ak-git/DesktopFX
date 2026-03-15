package com.ak.rsm2;

import java.util.function.DoubleUnaryOperator;

enum Sign implements DoubleUnaryOperator {
  PLUS(1), MINUS(-1);

  private final int sign;

  Sign(int sign) {
    this.sign = sign;
  }

  @Override
  public final double applyAsDouble(double operand) {
    return sign * operand;
  }
}
