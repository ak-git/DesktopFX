package com.ak.rsm.apparent;

import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractApparentRho implements ResistanceSumValue {
  @Nonnull
  private final ResistanceSumValue apparent;

  AbstractApparentRho(@Nonnull ResistanceSumValue apparent) {
    this.apparent = apparent;
  }

  @Override
  public final double value(@Nonnegative double hToL, @Nonnegative IntToDoubleFunction qn) {
    return apparent.value(hToL, qn);
  }

  @Override
  @Nonnull
  public final int sumFactor(@Nonnegative int n) {
    return apparent.sumFactor(n);
  }
}
