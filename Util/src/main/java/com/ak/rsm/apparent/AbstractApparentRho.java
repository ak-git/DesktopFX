package com.ak.rsm.apparent;

import java.util.Objects;
import java.util.function.IntToDoubleFunction;

abstract class AbstractApparentRho implements ResistanceSumValue {
  private final ResistanceSumValue apparent;

  AbstractApparentRho(ResistanceSumValue apparent) {
    this.apparent = Objects.requireNonNull(apparent);
  }

  @Override
  public final double value(double hToL, IntToDoubleFunction qn) {
    return apparent.value(hToL, qn);
  }

  @Override
  public final int sumFactor(int n) {
    return apparent.sumFactor(n);
  }
}
