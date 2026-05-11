package com.ak.rsm2;

import com.ak.math.ValuePair;

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
}
