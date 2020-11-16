package com.ak.rsm;

import javax.annotation.Nonnegative;

interface RelativeMediumLayers {
  double k12();

  @Nonnegative
  default double h() {
    return Double.NaN;
  }
}
