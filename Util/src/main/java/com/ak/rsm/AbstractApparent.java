package com.ak.rsm;

import javax.annotation.Nonnegative;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.pow;

abstract class AbstractApparent {
  @Nonnegative
  private final double sToL;
  @Nonnegative
  private final double Lh;

  AbstractApparent(@Nonnegative double sToL, @Nonnegative double Lh) {
    this.sToL = sToL;
    this.Lh = Lh;
  }

  final double sToL() {
    return sToL;
  }

  final double Lh() {
    return Lh;
  }

  final double electrodesFactor() {
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
