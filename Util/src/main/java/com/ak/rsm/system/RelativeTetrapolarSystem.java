package com.ak.rsm.system;

import static java.lang.StrictMath.pow;

public record RelativeTetrapolarSystem(double sToL, double x) {
  public static final double OPTIMAL_SL = 1.4142135623730951 - 1.0;
  public static final double MIN_ERROR_FACTOR = new RelativeTetrapolarSystem(OPTIMAL_SL).errorFactor();

  public RelativeTetrapolarSystem(double sToL) {
    this(Math.abs(sToL), Math.min(sToL, 1.0 / sToL));
  }

  public double factor(double sign) {
    return Math.abs(1.0 + Math.signum(sign) * sToL);
  }

  double errorFactor() {
    return (1.0 + x) / (x * (1.0 - x));
  }

  double hMaxFactor(double k) {
    double zeta3 = Math.abs(Layers.sum(n -> pow(k, n) / pow(n, 3.0)));
    double result = x * pow(1.0 - x, 2.0) * zeta3 / 32.0;
    return pow(result, 1.0 / 3.0);
  }

  double hMinFactor(double k) {
    if (Double.compare(k, 1.0) == 0) {
      return 0.0;
    }
    else {
      double result = 4.0;
      if (Double.compare(k, -1.0) != 0) {
        result = (1.0 + k) / (1.0 - k) / Math.abs(Layers.sum(n -> pow(k, n) * pow(n, 2.0)));
      }
      result *= (1.0 - x) * pow(1.0 + x, 3.0) / (x * (pow(x, 2.0) + 3.0));
      return Math.sqrt(result) / 4.0;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RelativeTetrapolarSystem that)) {
      return false;
    }

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

