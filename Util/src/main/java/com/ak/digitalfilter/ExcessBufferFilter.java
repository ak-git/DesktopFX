package com.ak.digitalfilter;

import javax.annotation.Nonnegative;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

final class ExcessBufferFilter extends AbstractBufferFilter {
  private final IntUnaryOperator operator;
  private long sum;
  private int length;

  private ExcessBufferFilter(@Nonnegative int size, IntUnaryOperator operator) {
    super(size + 1);
    this.operator = Objects.requireNonNull(operator);
  }

  @Nonnegative
  @Override
  public double getDelay() {
    return 0.0;
  }

  @Override
  int apply(@Nonnegative int nowIndex) {
    if (checkResetAndClear()) {
      sum = 0;
    }
    length = Math.min(length + 1, length() - 1);
    sum += operator.applyAsInt(get(nowIndex));
    sum -= operator.applyAsInt(get(nowIndex + 1));
    return (int) (sum / length);
  }

  @Override
  public String toString() {
    return toString(operator.toString());
  }

  static DigitalFilter mean(@Nonnegative int size) {
    return new ExcessBufferFilter(size, new IntUnaryOperator() {
      @Override
      public int applyAsInt(int operand) {
        return operand;
      }

      @Override
      public String toString() {
        return "MeanFilter";
      }
    });
  }

  static DigitalFilter std2(@Nonnegative int size) {
    return new ExcessBufferFilter(size, operand -> operand * operand);
  }
}