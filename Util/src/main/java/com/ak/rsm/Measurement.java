package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

interface Measurement {
  @Nonnull
  InexactTetrapolarSystem getSystem();

  @Nonnegative
  double getResistivity();

  default double getLogResistivity() {
    return StrictMath.log(getResistivity());
  }

  @Nonnull
  default Measurement merge(@Nonnull Measurement that) {
    throw new UnsupportedOperationException(that.toString());
  }
}
