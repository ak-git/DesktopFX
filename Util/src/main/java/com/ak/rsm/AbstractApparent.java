package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.pow;

abstract class AbstractApparent {
  @Nonnull
  private final double sToL;

  AbstractApparent(@Nonnegative double sToL) {
    this.sToL = sToL;
  }

  final double sToL() {
    return sToL;
  }

  final double electrodesFactor(@Nonnegative double Lh) {
    double v = Lh * (1.0 - pow(sToL(), 2.0));
    if (v > 0) {
      v /= sToL();
    }
    else {
      v = abs(v);
    }
    return v;
  }
}
