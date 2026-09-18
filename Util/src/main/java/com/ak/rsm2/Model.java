package com.ak.rsm2;

import com.ak.math.ValuePair;
import com.ak.util.Numbers;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed interface Model {
  record Layer2Relative(K k, double h) implements Model {
    public Layer2Relative {
      Objects.requireNonNull(k);
      if (h < 0) {
        throw new IllegalArgumentException("h = %f must be non-negative".formatted(h));
      }
    }

    @Override
    public String toString() {
      return Stream.of(ValuePair.Name.K12.of(k.value(), 0.0), ValuePair.Name.H.of(h, 0.0))
          .map(Objects::toString).collect(Collectors.joining("; ", "Layer2{", "}"));
    }
  }

  record Layer2RelativeDh(Layer2Relative layer2Relative, double dh) implements Model {
    public Layer2RelativeDh(K k, double h, double dh) {
      this(new Layer2Relative(k, h), dh);
    }

    @Override
    public String toString() {
      return Stream.of(layer2Relative, ValuePair.Name.DH.of(dh, 0.0))
          .map(Objects::toString).collect(Collectors.joining("; ", "Layer2{", "}"));
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

    public P multiply(double a) {
      return new P(a * p1, a * p2mp1);
    }
  }

  record Layer3Absolute(double rho1, double rho2, double rho3, double hStep, P p) implements Model {
    public Layer3Absolute {
      if (hStep < 0) {
        throw new IllegalArgumentException("hStep = %f must be non-negative".formatted(hStep));
      }
      Objects.requireNonNull(p);
    }

    public Model toModel(Model.P dp, double dRho2) {
      return new Model.Layer3Absolute(rho1, rho2 + dRho2, rho3, hStep, p.add(dp));
    }

    @Override
    public String toString() {
      return Stream.of(
              ValuePair.Name.RHO_1.of(rho1, 0.0), ValuePair.Name.RHO_2.of(rho2, 0.0), ValuePair.Name.RHO_3.of(rho3, 0.0),
              ValuePair.Name.H1.of(hStep * p.p1, 0.0), ValuePair.Name.H2.of(hStep * p.pSum(), 0.0)
          )
          .map(Objects::toString).collect(Collectors.joining("; "));
    }
  }

  record Layer3AbsoluteDRho2(Layer3Absolute layer3Absolute, P dp, double dRho2) implements Model {
    public Layer3AbsoluteDRho2 {
      Objects.requireNonNull(layer3Absolute);
      Objects.requireNonNull(dp);
    }

    @Override
    public String toString() {
      return Stream.of(layer3Absolute,
              ValuePair.Name.DH1.of(layer3Absolute.hStep * dp.p1, 0.0),
              ValuePair.Name.DH2.of(layer3Absolute.hStep * dp.pSum(), 0.0),
              ValuePair.Name.D_RHO_2.of(dRho2, 0.0)
          )
          .map(Objects::toString).collect(Collectors.joining("; "));
    }
  }
}
