package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class SqrtSumFilter extends AbstractExcessBufferFilter {
  SqrtSumFilter(@Nonnegative int n) {
    super(n);
  }

  @Override
  int add(int nowIndex) {
    return get(nowIndex) * get(nowIndex);
  }

  @Override
  int sub(int nowIndex) {
    return get(nowIndex + 1) * get(nowIndex + 1);
  }

  @Override
  int div() {
    return (int) Math.sqrt(super.div());
  }
}