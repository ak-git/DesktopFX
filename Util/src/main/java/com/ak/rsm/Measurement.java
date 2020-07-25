package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

interface Measurement {
  @Nonnegative
  double getResistivity();

  default double getLogResistivity() {
    return StrictMath.log(getResistivity());
  }

  @Nonnull
  TetrapolarSystem getSystem();
}
