package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class MeanFilter extends AbstractExcessBufferFilter {
  MeanFilter(@Nonnegative int averageFactor) {
    super(averageFactor);
  }

  @Override
  long add(long sum, int nowIndex) {
    return sum + get(nowIndex);
  }

  @Override
  long sub(long sum, int nowIndex) {
    return sum - get(nowIndex + 1);
  }

  @Override
  int div(long sum, @Nonnegative int length) {
    return (int) (sum / length);
  }
}