package com.ak.rsm2;

import com.ak.math.ValuePair;
import com.ak.util.Builder;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed interface IterativeModel {
  record Layer2Relative(K k, double h) implements IterativeModel {
    public Layer2Relative {
      if (h < 0) {
        throw new IllegalArgumentException("h = %f must be non-negative".formatted(h));
      }
    }

    public Layer2Relative(double[] variables) {
      this(K.of(variables[0]), variables[1]);
    }

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

    Model toModel(Model.P p, Model.P dp);

    static Step1 builder(double hStep, Model.P dp) {
      return new Layer3AbsoluteBuilder(hStep, dp);
    }

    sealed interface Step1 extends Builder<Layer3Absolute> {
      Builder<Layer3Absolute> variables(double rho1, double rho2, double rho3, Model.P p);

      Builder<Layer3Absolute> variables(double[] variables);
    }

    final class Layer3AbsoluteBuilder implements Step1 {
      private record Layer3AbsoluteRecord(double hStep, double rho1, double rho2, double rho3, Model.P p, Model.P dp)
          implements Layer3Absolute {
        Layer3AbsoluteRecord(double hStep, double[] variables) {
          this(hStep, variables[0], variables[1], variables[2],
              new Model.P(
                  Math.min(variables[3] / hStep, variables[4] / hStep),
                  Math.max(variables[3] / hStep, variables[4] / hStep)
              ),
              new Model.P(
                  Math.min(variables[5] / hStep, variables[6] / hStep),
                  Math.max(variables[5] / hStep, variables[6] / hStep)
              )
          );
        }

        @Override
        public Model toModel(Model.P p, Model.P dp) {
          return new Model.Layer3Absolute(rho1, rho2, rho3, hStep, p, p.add(dp));
        }

        @Override
        public String toString() {
          return toModel(p, dp).toString();
        }
      }

      private final double hStep;
      private final Model.P dp;
      private double @Nullable [] variables;
      private double rho1;
      private double rho2;
      private double rho3;
      private Model.@Nullable P p;

      private Layer3AbsoluteBuilder(double hStep, Model.P dp) {
        this.hStep = hStep;
        this.dp = dp;
      }

      @Override
      public Builder<Layer3Absolute> variables(double rho1, double rho2, double rho3, Model.P p) {
        this.rho1 = rho1;
        this.rho2 = rho2;
        this.rho3 = rho3;
        this.p = p;
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
          return new Layer3AbsoluteRecord(hStep, rho1, rho2, rho3, Objects.requireNonNull(p), dp);
        }
        else {
          return new Layer3AbsoluteRecord(hStep, variables);
        }
      }
    }
  }
}
