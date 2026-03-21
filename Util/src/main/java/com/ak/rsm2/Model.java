package com.ak.rsm2;

public interface Model {
  record Layer2Relative(K k, double h) {
    public Layer2Relative {
      if (h < 0) {
        throw new IllegalArgumentException("hSI = %f must be non-negative".formatted(h));
      }
    }

    public Layer2Relative(double k, double h) {
      this(K.of(k), h);
    }
  }
}
