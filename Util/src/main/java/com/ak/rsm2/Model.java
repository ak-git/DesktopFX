package com.ak.rsm2;

import com.ak.math.ValuePair;

import java.util.Arrays;
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

  record Layer3Relative(K k12, K k23, double hStep, P p, P pAfter) implements Model {
    record P(int p1, int p2mp1) {
      P {
        if (p1 < 0 || p2mp1 < 0) {
          throw new IllegalArgumentException("p1 = %d and p2mp1 = %d must be non-negative".formatted(p1, p2mp1));
        }
      }

      P(int[] p) {
        if (p.length != 2) {
          throw new IllegalArgumentException("p[%s].length != 2".formatted(Arrays.toString(p)));
        }
        this(p[0], p[1]);
      }

      public int p2() {
        return p1 + p2mp1;
      }
    }

    public Layer3Relative {
      if (hStep < 0) {
        throw new IllegalArgumentException("hStep = %f must be non-negative".formatted(hStep));
      }
    }

    @Override
    public String toString() {
      return Stream.of(
              ValuePair.Name.K12.of(k12.value(), 0.0), ValuePair.Name.K23.of(k23.value(), 0.0),
              ValuePair.Name.H1.of(hStep * p.p1, 0.0), ValuePair.Name.H2.of(hStep * p.p2(), 0.0)
          )
          .map(ValuePair::toString).collect(Collectors.joining("; "));
    }
  }
}
