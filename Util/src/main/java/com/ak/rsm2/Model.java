package com.ak.rsm2;

public interface Model {
  record Layer2Relative(double k, double hSI) {
    public Layer2Relative {
      k = Math.clamp(k, -1.0, 1.0);
      if (hSI < 0) {
        throw new IllegalArgumentException("hSI = %f must be non-negative".formatted(hSI));
      }
    }

    public Layer2Relative(K k, double hSI) {
      this(k.value(), hSI);
    }
  }
}
