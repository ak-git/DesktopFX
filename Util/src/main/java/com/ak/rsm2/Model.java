package com.ak.rsm2;

import com.ak.math.ValuePair;

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

    public Layer2Relative(double[] point) {
      this(point[0], point[1]);
    }

    @Override
    public String toString() {
      return "%s; %s".formatted(ValuePair.Name.K12.of(k.value(), 0.0), ValuePair.Name.H.of(h, 0.0));
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

    public Lung(double[] point) {
      this(point[0], point[1], point[2], point[3]);
    }
  }
}
