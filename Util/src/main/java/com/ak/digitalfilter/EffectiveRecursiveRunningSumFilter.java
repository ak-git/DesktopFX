package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class EffectiveRecursiveRunningSumFilter extends AbstractBufferFilter {
  @Nonnegative
  private final int powOfTwo;
  private int sum;

  EffectiveRecursiveRunningSumFilter(@Nonnegative int powOfTwo) {
    super(1 << powOfTwo);
    this.powOfTwo = powOfTwo;
  }

  @Override
  int apply(@Nonnegative int nowIndex) {
    sum += get(nowIndex) - get(nowIndex + 1);
    return sum >> powOfTwo;
  }
}
