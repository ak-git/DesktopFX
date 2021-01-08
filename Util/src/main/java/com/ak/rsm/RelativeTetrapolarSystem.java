package com.ak.rsm;

import javax.annotation.Nonnegative;

final class RelativeTetrapolarSystem {
  public static final double OPTIMAL_SL = 1.4142135623730951 - 1.0;
  public static final double MIN_ERROR_FACTOR = new RelativeTetrapolarSystem(OPTIMAL_SL).errorFactor();
  @Nonnegative
  private final double sToL;

  RelativeTetrapolarSystem(@Nonnegative double sToL) {
    this.sToL = Math.abs(sToL);
  }

  @Nonnegative
  double factor(double sign) {
    return Math.abs(1.0 + Math.signum(sign) * sToL);
  }

  double errorFactor() {
    double x = Math.min(sToL, 1.0 / sToL);
    return (1.0 + x) / (x * (1.0 - x));
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

