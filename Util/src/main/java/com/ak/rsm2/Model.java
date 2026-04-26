package com.ak.rsm2;

import com.ak.math.ValuePair;

import java.util.stream.Collectors;
import java.util.stream.Stream;

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
      return Stream.of(ValuePair.Name.K12.of(k.value(), 0.0), ValuePair.Name.H.of(h, 0.0))
          .map(ValuePair::toString).collect(Collectors.joining("; "));
    }
  }

  record Layer2Absolute(double rho1, double rho2, double h, double dh) implements Model {
    public Layer2Absolute {
      if (h < 0) {
        throw new IllegalArgumentException("h = %f must be non-negative".formatted(h));
      }
    }

    public Layer2Absolute(double[] point) {
      this(point[0], point[1], point[2], point[3]);
    }

    @Override
    public String toString() {
      return Stream.of(ValuePair.Name.RHO_1.of(rho1, 0.0), ValuePair.Name.RHO_2.of(rho2, 0.0),
              ValuePair.Name.H.of(h, 0.0), ValuePair.Name.DH.of(dh, 0.0))
          .map(ValuePair::toString).collect(Collectors.joining("; "));
    }
  }
}
