package com.ak.rsm;

import javax.annotation.Nonnegative;

import static java.lang.StrictMath.pow;

final class RelativeTetrapolarSystem {
  public static final double OPTIMAL_SL = 1.4142135623730951 - 1.0;
  public static final double MIN_ERROR_FACTOR = new RelativeTetrapolarSystem(OPTIMAL_SL).errorFactor();
  @Nonnegative
  private final double sToL;
  @Nonnegative
  private final double x;

  RelativeTetrapolarSystem(@Nonnegative double sToL) {
    this.sToL = Math.abs(sToL);
    x = Math.min(sToL, 1.0 / sToL);
  }

  @Nonnegative
  double factor(double sign) {
    return Math.abs(1.0 + Math.signum(sign) * sToL);
  }

  @Nonnegative
  double errorFactor() {
    return (1.0 + x) / (x * (1.0 - x));
  }

  @Nonnegative
  double hMaxFactor(double k) {
    double zeta3 = Math.abs(Layers.sum(n -> pow(k, n) / pow(n, 3.0)));
    double result = x * pow(1.0 - x, 2.0) * zeta3 / 32.0;
    return pow(result, 1.0 / 3.0);
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
    return Double.compare(x, that.x) == 0;
  }

  @Override
  public int hashCode() {
    return Double.hashCode(x);
  }

  @Override
  public String toString() {
    return "s / L = %.3f".formatted(sToL);
  }
}

