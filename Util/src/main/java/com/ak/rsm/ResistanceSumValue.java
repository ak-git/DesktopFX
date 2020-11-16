package com.ak.rsm;

import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface ResistanceSumValue {
  double value(@Nonnegative double h, @Nonnull IntToDoubleFunction qn);

  int sumFactor(@Nonnegative int n);
}
