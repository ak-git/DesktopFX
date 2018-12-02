package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

abstract class AbstractExcessBufferFilter extends AbstractBufferFilter {
  private long sum;
  private int length;

  AbstractExcessBufferFilter(@Nonnegative int size) {
    super(size + 1);
  }

  @Nonnegative
  @Override
  public final double getDelay() {
    return 0.0;
  }

  @Override
  final int apply(@Nonnegative int nowIndex) {
    if (checkResetAndClear()) {
      sum = 0;
    }
    length = Math.min(length + 1, length() - 1);
    sum = sub(add(sum, nowIndex), nowIndex);
    return div(sum, length);
  }

  abstract long add(long sum, @Nonnegative int nowIndex);

  abstract long sub(long sum, @Nonnegative int nowIndex);

  abstract int div(long sum, @Nonnegative int length);
}