package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

abstract class AbstractExcessBufferFilter extends AbstractBufferFilter {
  private long sum;
  private int length;

  AbstractExcessBufferFilter(@Nonnegative int size) {
    super(size + 1);
  }

  @Override
  final int apply(@Nonnegative int nowIndex) {
    if (checkResetAndClear()) {
      sum = 0;
    }
    length = Math.min(length + 1, length());
    sum += add(nowIndex);
    sum -= sub(nowIndex);
    return div();
  }

  abstract int add(@Nonnegative int nowIndex);

  abstract int sub(@Nonnegative int nowIndex);

  int div() {
    return (int) (sum / length);
  }

  @Override
  final int length() {
    return buffer().length - 1;
  }
}