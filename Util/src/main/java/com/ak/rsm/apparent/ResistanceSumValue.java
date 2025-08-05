package com.ak.rsm.apparent;

import java.util.function.IntToDoubleFunction;

interface ResistanceSumValue {
  double value(double hToL, IntToDoubleFunction qn);

  int sumFactor(int n);
}
