package com.ak.rsm2;

import com.ak.math.ValuePair;
import com.ak.util.Numbers;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed interface Model {
  sealed interface Layer2Model extends Model {
    K k();

    double h();
  }

  record Layer2Relative(K k, double h) implements Layer2Model {
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

  sealed interface Layer2DhModel extends Model {
    Layer2Model relative();

    double dh();
  }

  record Layer2RelativeDh(Layer2Model relative, double dh) implements Layer2DhModel {
    public Layer2RelativeDh {
      Objects.requireNonNull(relative);
    }

    public Layer2RelativeDh(K k, double h, double dh) {
      this(new Layer2Relative(k, h), dh);
    }

    @Override
    public String toString() {
      return Stream.of(relative, ValuePair.Name.DH.of(dh, 0.0))
          .map(Objects::toString).collect(Collectors.joining("; ", "{", "}"));
    }
  }

  record Layer2Absolute(double rho1, double rho2, double h) implements Layer2Model {
    public Layer2Absolute {
      if (rho1 < 0 || rho2 < 0 || h < 0) {
        throw new IllegalArgumentException("rho1 = %f; rho2 = %f; h = %f must be non-negative".formatted(rho1, rho2, h));
      }
    }

    @Override
    public String toString() {
      return Stream.of(ValuePair.Name.RHO_1.of(rho1, 0.0), ValuePair.Name.RHO_2.of(rho2, 0.0),
              ValuePair.Name.H.of(h, 0.0))
          .map(Objects::toString).collect(Collectors.joining("; ", "Layer2{", "}"));
    }

    @Override
    public K k() {
      return K.of(rho1, rho2);
    }
  }

  record Layer2AbsoluteDh(Layer2Absolute layer2Absolute, double dh) implements Layer2DhModel {
    public Layer2AbsoluteDh {
      Objects.requireNonNull(layer2Absolute);
    }

    public Layer2AbsoluteDh(double rho1, double rho2, double h, double dh) {
      this(new Layer2Absolute(rho1, rho2, h), dh);
    }

    @Override
    public String toString() {
      return Stream.of(layer2Absolute, ValuePair.Name.DH.of(dh, 0.0))
          .map(Objects::toString).collect(Collectors.joining("; ", "{", "}"));
    }

    @Override
    public Layer2Model relative() {
      return layer2Absolute;
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

  sealed interface Layer3Model extends Model {
    K k1();

    K k2();

    double hStep();

    P p();

    double h1();

    double h2();
  }

  record Layer3Relative(K k1, K k2, double hStep, P p) implements Layer3Model {
    public Layer3Relative {
      Objects.requireNonNull(k1);
      Objects.requireNonNull(k2);
      if (hStep < 0) {
        throw new IllegalArgumentException("hStep = %f must be non-negative".formatted(hStep));
      }
    }

    @Override
    public String toString() {
      return Stream.of(ValuePair.Name.K12.of(k1.value(), 0.0), ValuePair.Name.K23.of(k2.value(), 0.0),
              ValuePair.Name.H1.of(h1(), 0.0), ValuePair.Name.H2.of(h2(), 0.0))
          .map(Objects::toString).collect(Collectors.joining("; ", "Layer3{", "}"));
    }

    @Override
    public double h1() {
      return hStep * p.p1;
    }

    @Override
    public double h2() {
      return hStep * p.pSum();
    }
  }

  sealed interface Layer3DhModel extends Model {
    Layer3Model relative();

    P dp();
  }

  record Layer3RelativeDh(Layer3Relative relative, P dp) implements Layer3DhModel {
    public Layer3RelativeDh {
      Objects.requireNonNull(relative);
      Objects.requireNonNull(dp);
    }

    @Override
    public String toString() {
      return Stream.of(relative,
              ValuePair.Name.DH1.of(relative.hStep * dp.p1, 0.0),
              ValuePair.Name.DH2.of(relative.hStep * dp.pSum(), 0.0))
          .map(Objects::toString).collect(Collectors.joining("; ", "{", "}"));
    }
  }

  record Layer3Absolute(double rho1, double rho2, double rho3, double hStep, P p) implements Layer3Model {
    public Layer3Absolute {
      if (hStep < 0) {
        throw new IllegalArgumentException("hStep = %f must be non-negative".formatted(hStep));
      }
      Objects.requireNonNull(p);
    }

    public Model toModel(Model.P dp, double dRho1, double dRho2) {
      return new Model.Layer3Absolute(rho1 + dRho1, rho2 + dRho2, rho3, hStep, p.add(dp));
    }

    @Override
    public String toString() {
      return Stream.of(
              ValuePair.Name.RHO_1.of(rho1, 0.0), ValuePair.Name.RHO_2.of(rho2, 0.0), ValuePair.Name.RHO_3.of(rho3, 0.0),
              ValuePair.Name.H1.of(h1(), 0.0), ValuePair.Name.H2.of(h2(), 0.0)
          )
          .map(Objects::toString).collect(Collectors.joining("; "));
    }

    @Override
    public K k1() {
      return K.of(rho1, rho2);
    }

    @Override
    public K k2() {
      return K.of(rho2, rho3);
    }

    @Override
    public double h1() {
      return hStep * p.p1;
    }

    @Override
    public double h2() {
      return hStep * p.pSum();
    }
  }

  record Layer3AbsoluteDhDRho(Layer3Absolute layer3Absolute, P dp, double dRho1, double dRho2) implements Model {
    public Layer3AbsoluteDhDRho {
      Objects.requireNonNull(layer3Absolute);
      Objects.requireNonNull(dp);
    }

    @Override
    public String toString() {
      return Stream.of(layer3Absolute,
              ValuePair.Name.DH1.of(layer3Absolute.hStep * dp.p1, 0.0),
              ValuePair.Name.DH2.of(layer3Absolute.hStep * dp.pSum(), 0.0),
              ValuePair.Name.D_RHO_1.of(dRho1, 0.0),
              ValuePair.Name.D_RHO_2.of(dRho2, 0.0)
          )
          .map(Objects::toString).collect(Collectors.joining("; "));
    }
  }
}
