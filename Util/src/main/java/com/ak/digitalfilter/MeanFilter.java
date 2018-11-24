package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class MeanFilter extends AbstractExcessBufferFilter {
  private long mean;
  private int length;

  MeanFilter(@Nonnegative int averageFactor) {
    super(averageFactor);
  }

  @Override
  int apply(@Nonnegative int nowIndex) {
    if (checkResetAndClear()) {
      mean = 0;
    }
    length = Math.min(length + 1, length() - 1);
    mean += get(nowIndex);
    mean -= get(nowIndex + 1);
    return (int) (mean / length);
  }
}