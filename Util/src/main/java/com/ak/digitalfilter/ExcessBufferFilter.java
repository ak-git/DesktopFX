package com.ak.digitalfilter;

import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class ExcessBufferFilter extends AbstractBufferFilter {
  private final IntUnaryOperator operator;
  private final String name;
  private long sum;
  private int length;

  private ExcessBufferFilter(@Nonnegative int size, @Nonnull IntUnaryOperator operator, @Nonnull String name) {
    super(size + 1);
    this.operator = operator;
    this.name = name;
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
    return toString(name);
  }

  static DigitalFilter mean(@Nonnegative int size) {
    return new ExcessBufferFilter(size, IntUnaryOperator.identity(), "MeanFilter");
  }

  static DigitalFilter std2(@Nonnegative int size) {
    return new ExcessBufferFilter(size, x -> x * x, "Std2Filter");
  }
}