package com.ak.rsm;

import javax.annotation.Nonnegative;

class RelativeTetrapolarSystem {
  @Nonnegative
  private final double sToL;

  RelativeTetrapolarSystem(@Nonnegative double sToL) {
    this.sToL = Math.abs(sToL);
  }

  @Nonnegative
  public final double factor(double sign) {
    return Math.abs(1.0 + Math.signum(sign) * sToL);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RelativeTetrapolarSystem)) {
      return false;
    }

    RelativeTetrapolarSystem that = (RelativeTetrapolarSystem) o;
    return Double.compare(Math.min(sToL, 1.0 / sToL), Math.min(that.sToL, 1.0 / that.sToL)) == 0;
  }

  @Override
  public int hashCode() {
    return Double.hashCode(Math.min(sToL, 1.0 / sToL));
  }

  @Override
  public String toString() {
    return "s / L = %.3f".formatted(sToL);
  }
}

