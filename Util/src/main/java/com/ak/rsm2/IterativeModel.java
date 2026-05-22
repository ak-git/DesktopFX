package com.ak.rsm2;

import com.ak.math.ValuePair;
import com.ak.util.Numbers;

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

  record Layer3Relative(double hStep, K k12, K k23, Model.Layer3Relative.P p, Model.Layer3Relative.P dp,
                        int dpFat) implements IterativeModel {
    public Layer3Relative(double hStep, double[] variables) {
      this(hStep, K.of(variables[0]), K.of(variables[1]),
          new Model.Layer3Relative.P(variables[2] / hStep, variables[3] / hStep),
          new Model.Layer3Relative.P(variables[4] / hStep, variables[5] / hStep),
          Numbers.toInt(variables[6] / hStep)
      );
    }

    @Override
    public Model toModel() {
      return new Model.Layer3Relative(k12, k23, hStep, p,
          new Model.Layer3Relative.P(p.p1() + dp.p1(), p.p2mp1() + dp.p2mp1())
      );
    }

    public double dh() {
      return (dp.p1() + dp.p2mp1()) * hStep;
    }

    public Model toModelAfterFat() {
      return new Model.Layer3Relative(k12, k23, hStep,
          new Model.Layer3Relative.P(p.p1(), p.p2mp1() + dpFat),
          new Model.Layer3Relative.P(p.p1() + dp.p1(), p.p2mp1() + dpFat + dp.p2mp1())
      );
    }

    public Model toModelDiffFat() {
      return new Model.Layer3Relative(k12, k23, hStep, p,
          new Model.Layer3Relative.P(p.p1(), p.p2mp1() + dpFat)
      );
    }

    public double dhDiffFat() {
      return dpFat * hStep;
    }
  }
}
