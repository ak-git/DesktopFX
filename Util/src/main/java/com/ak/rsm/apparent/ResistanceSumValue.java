package com.ak.rsm.apparent;

import javax.annotation.Nonnegative;
import java.util.function.IntToDoubleFunction;

interface ResistanceSumValue {
  double value(@Nonnegative double hToL, IntToDoubleFunction qn);

  int sumFactor(@Nonnegative int n);
}
