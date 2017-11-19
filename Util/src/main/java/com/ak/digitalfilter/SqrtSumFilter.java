package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class SqrtSumFilter extends AbstractBufferFilter {
  private final int n;
  private long sumSqr;

  SqrtSumFilter(@Nonnegative int n) {
    super(n + 1);
    this.n = n;
  }

  @Override
  int apply(@Nonnegative int nowIndex) {
    sumSqr += get(nowIndex) * get(nowIndex);
    sumSqr -= get(nowIndex + 1) * get(nowIndex + 1);
    return (int) Math.sqrt(1.0 * sumSqr / n);
  }
}