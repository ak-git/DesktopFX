package com.ak.rsm;

import javax.annotation.Nonnegative;

interface RelativeMediumLayers {
  RelativeMediumLayers SINGLE_LAYER = () -> 0;

  double k12();

  @Nonnegative
  default double h() {
    return Double.NaN;
  }
}
