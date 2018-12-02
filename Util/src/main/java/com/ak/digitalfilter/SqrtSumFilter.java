package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class SqrtSumFilter extends AbstractExcessBufferFilter {
  SqrtSumFilter(@Nonnegative int n) {
    super(n);
  }

  @Override
  long add(long sum, int nowIndex) {
    long x = get(nowIndex);
    return sum + x * x;
  }

  @Override
  long sub(long sum, int nowIndex) {
    long x = get(nowIndex + 1);
    return sum - x * x;
  }

  @Override
  int div(long sum, @Nonnegative int length) {
    return (int) (Math.sqrt(sum / Math.max(length - 1.0, 1.0)));
  }
}