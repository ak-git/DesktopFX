package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class CombFilter extends AbstractBufferFilter {
  CombFilter(@Nonnegative int combFactor) {
    super(combFactor + 1);
  }

  @Override
  int apply(@Nonnegative int nowIndex) {
    return get(nowIndex) - get(nowIndex + 1);
  }
}