package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class MeanFilter extends AbstractExcessBufferFilter {
  MeanFilter(@Nonnegative int averageFactor) {
    super(averageFactor);
  }

  @Override
  int add(int nowIndex) {
    return get(nowIndex);
  }

  @Override
  int sub(int nowIndex) {
    return get(nowIndex + 1);
  }
}