package com.ak.rsm2;

import com.ak.math.ValuePair;
import com.ak.util.Numbers;

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

  record P(int p1, int p2mp1) {
    P(int[] p) {
      if (p.length != 2) {
        throw new IllegalArgumentException("p[%s].length != 2".formatted(Arrays.toString(p)));
      }
      this(p[0], p[1]);
    }

    P(double p1, double p2mp1) {
      this(Numbers.toInt(p1), Numbers.toInt(p2mp1));
    }

    public P {
      if (p1 < 0 || p2mp1 < 0) {
        throw new IllegalArgumentException("p = [%d; %d] must be non-negative".formatted(p1, p2mp1));
      }
    }

    public int pSum() {
      return p1 + p2mp1;
    }

    public P add(P p) {
      return new P(p1 + p.p1, p2mp1 + p.p2mp1);
    }
  }

  record Layer3Absolute(double rho1, double rho2, double rho3, double hStep, P p, P pAfter) implements Model {
    public Layer3Absolute {
      if (hStep < 0) {
        throw new IllegalArgumentException("hStep = %f must be non-negative".formatted(hStep));
      }
    }

    @Override
    public String toString() {
      return Stream.of(
              ValuePair.Name.RHO_1.of(rho1, 0.0), ValuePair.Name.RHO_2.of(rho2, 0.0), ValuePair.Name.RHO_3.of(rho3, 0.0),
              ValuePair.Name.H1.of(hStep * p.p1, 0.0), ValuePair.Name.H2.of(hStep * p.pSum(), 0.0),
              ValuePair.Name.DH1.of(hStep * (pAfter.p1 - p.p1), 0.0), ValuePair.Name.DH2.of(hStep * (pAfter.pSum() - p.pSum()), 0.0)
          )
          .map(ValuePair::toString).collect(Collectors.joining("; "));
    }
  }
}
