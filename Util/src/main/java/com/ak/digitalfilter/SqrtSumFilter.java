package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class SqrtSumFilter extends AbstractBufferFilter {
  private long sumSqr;

  SqrtSumFilter(@Nonnegative int n) {
    super(n + 1);
  }

  @Override
  int apply(@Nonnegative int nowIndex) {
    sumSqr += get(nowIndex) * get(nowIndex);
    sumSqr -= get(nowIndex + 1) * get(nowIndex + 1);
    return (int) Math.sqrt(1.0 * sumSqr / (length() - 1));
  }
}