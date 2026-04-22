package com.ak.rsm2;

public sealed interface Model {
  record Layer2Relative(K k, double h) implements Model {
    public Layer2Relative {
      if (h < 0) {
        throw new IllegalArgumentException("h = %f must be non-negative".formatted(h));
      }
    }

    public Layer2Relative(double k, double h) {
      this(K.of(k), h);
    }
  }

  record Lung(double rho1, double rho2Expiration, double rho2Inspiration, double h) implements Model {
    public Lung {
      if (rho1 < 0) {
        throw new IllegalArgumentException("rho1 = %f must be non-negative".formatted(rho1));
      }
      if (rho2Expiration < 0) {
        throw new IllegalArgumentException("rho2Expiration = %f must be non-negative".formatted(rho2Expiration));
      }
      if (rho2Inspiration < 0) {
        throw new IllegalArgumentException("rho2Inspiration = %f must be non-negative".formatted(rho2Inspiration));
      }
      if (h < 0) {
        throw new IllegalArgumentException("h = %f must be non-negative".formatted(h));
      }
      if (rho2Inspiration < rho2Expiration) {
        throw new IllegalArgumentException("rho2Inspiration = %f must be > then rho2Expiration = %f".formatted(rho2Inspiration, rho2Expiration));
      }
    }
  }
}
