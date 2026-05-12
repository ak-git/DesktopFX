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

    @Override
    public String toString() {
      return Stream.of(ValuePair.Name.K12.of(k.value(), 0.0), ValuePair.Name.H.of(h, 0.0))
          .map(ValuePair::toString).collect(Collectors.joining("; "));
    }
  }

  record Layer3Relative(K k12, K k23, double hStep, int p1, int p2mp1) implements Model {
    public Layer3Relative {
      if (hStep < 0) {
        throw new IllegalArgumentException("hStep = %f must be non-negative".formatted(hStep));
      }
    }

    @Override
    public String toString() {
      return Stream.of(
              ValuePair.Name.K12.of(k12.value(), 0.0), ValuePair.Name.K23.of(k23.value(), 0.0),
              ValuePair.Name.H1.of(hStep * p1, 0.0), ValuePair.Name.H2.of(hStep * (p1 + p2mp1), 0.0)
          )
          .map(ValuePair::toString).collect(Collectors.joining("; "));
    }
  }
}
