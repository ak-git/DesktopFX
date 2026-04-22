package com.ak.rsm2;

public sealed interface K {
  K PLUS_ONE = new KRecord(1.0);
  K ZERO = new KRecord(0.0);
  K MINUS_ONE = new KRecord(-1.0);

  double value();

  boolean isPlusOne();

  boolean isZero();

  boolean isMinusOne();

  static K of(double value) {
    return new KRecord(value);
  }

  static K of(double rho1, double rho2) {
    if (Double.compare(rho1, rho2) == 0) {
      return ZERO;
    }
    else if (Double.isInfinite(rho2)) {
      return PLUS_ONE;
    }
    else if (Double.isInfinite(rho1)) {
      return MINUS_ONE;
    }
    else {
      return new KRecord((rho2 - rho1) / (rho2 + rho1));
    }
  }

  record KRecord(double value) implements K {
    public KRecord {
      value = Math.clamp(value, -1.0, 1.0);
    }

    @Override
    public boolean isPlusOne() {
      return Double.compare(value, 1.0) == 0;
    }

    @Override
    public boolean isZero() {
      return Double.compare(value, 0.0) == 0;
    }

    @Override
    public boolean isMinusOne() {
      return Double.compare(value, -1.0) == 0;
    }
  }
}
