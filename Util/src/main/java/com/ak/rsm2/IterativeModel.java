package com.ak.rsm2;

import com.ak.math.ValuePair;
import com.ak.util.Builder;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed interface IterativeModel {
  Model toModel();

  record Layer2Relative(K k, double h) implements IterativeModel {
    public Layer2Relative {
      if (h < 0) {
        throw new IllegalArgumentException("h = %f must be non-negative".formatted(h));
      }
    }

    public Layer2Relative(double[] variables) {
      this(K.of(variables[0]), variables[1]);
    }

    @Override
    public Model toModel() {
      return new Model.Layer2Relative(k, h);
    }

    @Override
    public String toString() {
      return toModel().toString();
    }
  }

  record Layer2RelativeDh(K k, double h, double dh) implements IterativeModel {
    public Layer2RelativeDh {
      if (h < 0) {
        throw new IllegalArgumentException("h = %f must be non-negative".formatted(h));
      }
    }

    public Layer2RelativeDh(double[] variables) {
      this(K.of(variables[0]), variables[1], variables[2]);
    }

    @Override
    public Model toModel() {
      return new Model.Layer2Relative(k, h);
    }

    @Override
    public String toString() {
      return Stream.of(ValuePair.Name.K12.of(k.value(), 0.0), ValuePair.Name.H.of(h, 0.0),
              ValuePair.Name.DH.of(dh, 0.0))
          .map(ValuePair::toString).collect(Collectors.joining("; "));
    }
  }

  sealed interface Layer3Absolute extends IterativeModel {
    double hStep();

    double rho1();

    double rho2();

    double rho3();

    Model.P p();

    Model.P dp();

    double dRho2();

    Model toModel(Model.P dp, double dRho2);

    static Step1 builder(double hStep) {
      return new Layer3AbsoluteBuilder(hStep);
    }

    sealed interface Step1 extends Builder<Layer3Absolute> {
      Builder<Layer3Absolute> variables(double rho1, double rho2, double rho3, Model.P p, Model.P dp, double dRho2);

      Builder<Layer3Absolute> variables(double[] variables);
    }

    final class Layer3AbsoluteBuilder implements Step1 {
      private record Layer3AbsoluteRecord(double hStep, double rho1, double rho2, double rho3, Model.P p, Model.P dp,
                                          double dRho2)
          implements Layer3Absolute {
        Layer3AbsoluteRecord(double hStep, double[] variables) {
          this(hStep, variables[0], variables[1], variables[2],
              new Model.P(Math.min(variables[3] / hStep, variables[4] / hStep), Math.max(variables[3] / hStep, variables[4] / hStep)),
              new Model.P(Math.min(variables[5] / hStep, variables[6] / hStep), Math.max(variables[5] / hStep, variables[6] / hStep)),
              variables[7]
          );
        }

        @Override
        public Model toModel() {
          return new Model.Layer3Absolute(rho1, rho2, rho3, hStep, p);
        }

        @Override
        public Model toModel(Model.P dp, double dRho2) {
          return new Model.Layer3Absolute(rho1, rho2 + dRho2, rho3, hStep, p.add(dp));
        }

        @Override
        public String toString() {
          return Stream.of(
                  ValuePair.Name.RHO_1.of(rho1, 0.0), ValuePair.Name.RHO_2.of(rho2, 0.0), ValuePair.Name.RHO_3.of(rho3, 0.0),
                  ValuePair.Name.H1.of(hStep * p.p1(), 0.0),
                  ValuePair.Name.H2.of(hStep * p.pSum(), 0.0),
                  ValuePair.Name.DH1.of(hStep * (dp.p1()), 0.0),
                  ValuePair.Name.DH2.of(hStep * (dp.pSum()), 0.0),
                  ValuePair.Name.D_RHO_2.of(dRho2, 0.0)
              )
              .map(ValuePair::toString).collect(Collectors.joining("; "));
        }
      }

      private final double hStep;
      private double @Nullable [] variables;
      private double rho1;
      private double rho2;
      private double rho3;
      private Model.@Nullable P p;
      private Model.@Nullable P dp;
      private double dRho2;

      private Layer3AbsoluteBuilder(double hStep) {
        this.hStep = hStep;
      }

      @Override
      public Builder<Layer3Absolute> variables(double rho1, double rho2, double rho3, Model.P p, Model.P dp, double dRho2) {
        this.rho1 = rho1;
        this.rho2 = rho2;
        this.rho3 = rho3;
        this.p = p;
        this.dp = dp;
        this.dRho2 = dRho2;
        return this;
      }

      @Override
      public Builder<Layer3Absolute> variables(double[] variables) {
        this.variables = variables.clone();
        return this;
      }

      @Override
      public Layer3Absolute build() {
        if (variables == null) {
          return new Layer3AbsoluteRecord(hStep, rho1, rho2, rho3, Objects.requireNonNull(p), Objects.requireNonNull(dp), dRho2);
        }
        else {
          return new Layer3AbsoluteRecord(hStep, variables);
        }
      }
    }
  }
}
