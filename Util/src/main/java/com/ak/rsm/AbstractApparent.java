package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractApparent {
  @Nonnull
  private final double sToL;

  AbstractApparent(@Nonnegative double sToL) {
    this.sToL = sToL;
  }

  final double sToL() {
    return sToL;
  }
}
