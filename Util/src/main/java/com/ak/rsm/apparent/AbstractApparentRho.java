package com.ak.rsm.apparent;

import javax.annotation.Nonnegative;
import java.util.Objects;
import java.util.function.IntToDoubleFunction;

abstract class AbstractApparentRho implements ResistanceSumValue {
  private final ResistanceSumValue apparent;

  AbstractApparentRho(ResistanceSumValue apparent) {
    this.apparent = Objects.requireNonNull(apparent);
  }

  @Override
  public final double value(@Nonnegative double hToL, @Nonnegative IntToDoubleFunction qn) {
    return apparent.value(hToL, qn);
  }

  @Override
  public final int sumFactor(@Nonnegative int n) {
    return apparent.sumFactor(n);
  }
}
