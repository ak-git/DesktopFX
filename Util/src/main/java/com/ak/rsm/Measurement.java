package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

interface Measurement {
  @Nonnegative
  double getResistivity();

  double getLogResistivity();

  @Nonnull
  TetrapolarSystem getSystem();
}
