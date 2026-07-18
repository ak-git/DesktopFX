package com.ak.rsm2;

import com.ak.math.ValuePair;
import com.ak.util.Builder;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed interface IterativeModel {
  record Layer2Absolute(double rho1, double rho2, double h) implements IterativeModel {
    public Layer2Absolute {
      if (rho1 < 0 || rho2 < 0 || h < 0) {
        throw new IllegalArgumentException("all variables [%f; %f; %f] must be non-negative".formatted(rho1, rho2, h));
      }
    }

    public Layer2Absolute(double[] variables) {
      this(variables[0], variables[1], variables[2]);
    }

    public Model toModel() {
      return new Model.Layer2Absolute(rho1, rho2, h);
    }

    @Override
    public String toString() {
      return toModel().toString();
    }
  }

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

  sealed interface Layer3Relative extends IterativeModel {
    double hStep();

    K k12();

    K k23();

    Model.Layer3Relative.P p();

    Model.Layer3Relative.P dp();

    Model toModel(Model.Layer3Relative.P p, Model.Layer3Relative.P dp);

    static Step1 builder(double hStep, Model.Layer3Relative.P dp) {
      return new Layer3RelativeBuilder(hStep, dp);
    }

    sealed interface Step1 extends Builder<Layer3Relative> {
      Builder<Layer3Relative> variables(K k12, K k23, Model.Layer3Relative.P p);

      Builder<Layer3Relative> variables(double[] variables);
    }

    final class Layer3RelativeBuilder implements Step1 {
      private record Layer3RelativeRecord(double hStep, K k12, K k23, Model.Layer3Relative.P p,
                                          Model.Layer3Relative.P dp) implements Layer3Relative {
        Layer3RelativeRecord(double hStep, double[] variables) {
          this(hStep, K.of(variables[0]), K.of(variables[1]),
              new Model.Layer3Relative.P(
                  Math.min(variables[2] / hStep, variables[3] / hStep),
                  Math.max(variables[2] / hStep, variables[3] / hStep)
              ),
              new Model.Layer3Relative.P(
                  Math.min(variables[4] / hStep, variables[5] / hStep),
                  Math.max(variables[4] / hStep, variables[5] / hStep)
              )
          );
        }

        @Override
        public Model toModel(Model.Layer3Relative.P p, Model.Layer3Relative.P dp) {
          return new Model.Layer3Relative(k12, k23, hStep, p, p.add(dp));
        }

        @Override
        public String toString() {
          return toModel(p, dp).toString();
        }
      }

      private final double hStep;
      private final Model.Layer3Relative.P dp;
      private double @Nullable [] variables;
      private @Nullable K k12;
      private @Nullable K k23;
      private Model.Layer3Relative.@Nullable P p;

      private Layer3RelativeBuilder(double hStep, Model.Layer3Relative.P dp) {
        this.hStep = hStep;
        this.dp = dp;
      }

      @Override
      public Builder<Layer3Relative> variables(K k12, K k23, Model.Layer3Relative.P p) {
        this.k12 = k12;
        this.k23 = k23;
        this.p = p;
        return this;
      }

      @Override
      public Builder<Layer3Relative> variables(double[] variables) {
        this.variables = variables.clone();
        return this;
      }

      @Override
      public Layer3Relative build() {
        if (variables == null) {
          return new Layer3RelativeRecord(hStep, Objects.requireNonNull(k12), Objects.requireNonNull(k23), Objects.requireNonNull(p),
              dp);
        }
        else {
          return new Layer3RelativeRecord(hStep, variables);
        }
      }
    }
  }
}
