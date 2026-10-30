package com.ak.rsm2;

import com.ak.rsm.system.Layers;
import com.ak.util.Builder;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

public sealed interface Resistivity {
  ElectrodeSystem.Tetrapolar system();

  static Step1 of(ElectrodeSystem.Tetrapolar system) {
    return new ResistivityBuilder(system);
  }

  sealed interface Step1 {
    Apparent apparent(Model model);

    double apparent(double rOhm);
  }

  final class ResistivityBuilder implements Step1 {
    private final ElectrodeSystem.Tetrapolar tetrapolar;

    private ResistivityBuilder(ElectrodeSystem.Tetrapolar tetrapolar) {
      this.tetrapolar = Objects.requireNonNull(tetrapolar);
    }

    @Override
    public Apparent apparent(Model model) {
      return new Apparent.ApparentBuilder(tetrapolar, model).build();
    }

    @Override
    public double apparent(double rOhm) {
      return (Math.PI / 2.0) * rOhm / tetrapolar.phiFactor();
    }
  }

  sealed interface Apparent extends Resistivity {
    double value();

    double derivative();

    final class ApparentBuilder implements Builder<Apparent> {
      private record ApparentRecord(ElectrodeSystem.Tetrapolar system, double value, double derivative)
          implements Apparent {
        private ApparentRecord {
          Objects.requireNonNull(system);
        }

        @Override
        public ElectrodeSystem.Tetrapolar system() {
          return system;
        }
      }

      private enum Sign implements DoubleUnaryOperator {
        PLUS(1), MINUS(-1);

        private final int signCoeff;

        Sign(int signCoeff) {
          this.signCoeff = signCoeff;
        }

        @Override
        public final double applyAsDouble(double operand) {
          return signCoeff * operand;
        }
      }

      private final ElectrodeSystem.Tetrapolar system;
      private final Model model;

      private ApparentBuilder(ElectrodeSystem.Tetrapolar system, Model model) {
        this.system = Objects.requireNonNull(system);
        this.model = Objects.requireNonNull(model);
      }

      @Override
      public Apparent build() {
        return switch (model) {
          case Model.Layer2Relative layer2Relative -> layer2RelativeApparent(layer2Relative);
          case Model.Layer2RelativeDh layer2RelativeDh -> layer2RelativeApparent(layer2RelativeDh.relative());
          case Model.Layer2Absolute layer2Absolute -> layer2AbsoluteApparent(layer2Absolute);
          case Model.Layer2AbsoluteDh layer2AbsoluteDh -> layer2AbsoluteApparent(layer2AbsoluteDh.layer2Absolute());
          case Model.Layer3Relative layer3Relative -> layer3RelativeApparent(layer3Relative);
          case Model.Layer3RelativeDh(Model.Layer3Relative relative, Model.P dp) -> {
            Model.Layer3Model relative2 = new Model.Layer3Relative(relative.k1(), relative.k2(), relative.hStep(), relative.p().add(dp));
            Apparent apparent = layer3RelativeApparent(relative);
            Apparent apparent2 = layer3RelativeApparent(relative2);
            double derivative = (apparent2.value() - apparent.value()) / (dp.pSum() * relative.hStep());
            yield new ApparentRecord(system, apparent.value(), derivative / system.phiFactor());
          }
          case Model.Layer3Absolute layer3Absolute -> layer3AbsoluteApparent(layer3Absolute);
          case Model.Layer3AbsoluteDhDRho layer3AbsoluteDhDRho ->
              throw new IllegalArgumentException(layer3AbsoluteDhDRho.toString());
        };
      }

      private DoubleUnaryOperator braceOperation(double hSI, DoubleUnaryOperator sign) {
        return n -> {
          double nom = 1.0 + sign.applyAsDouble(system.sToL());
          double den = 1.0 + sign.andThen(Sign.MINUS).applyAsDouble(system.sToL());
          double left = 1.0 - Math.abs(nom / den);
          double right = 4.0 * n * hSI * system.phiFactor();
          return 1.0 / hypot(left, right);
        };
      }

      private Apparent layer2RelativeApparent(Model.Layer2Model layer2Relative) {
        double k = layer2Relative.k().value();
        double h = layer2Relative.h();
        DoubleUnaryOperator left = braceOperation(h, Sign.MINUS);
        DoubleUnaryOperator right = braceOperation(h, Sign.PLUS);
        return new ApparentRecord(system,
            1.0 + 2.0 * Layers.sum(n -> pow(k, n) * (left.applyAsDouble(n) - right.applyAsDouble(n))),
            -32.0 * h * system.phiFactor() *
                Layers.sum(n -> pow(k, n) * n * n * (pow(left.applyAsDouble(n), 3.0) - pow(right.applyAsDouble(n), 3.0)))
        );
      }

      private Apparent layer2AbsoluteApparent(Model.Layer2Absolute layer2Absolute) {
        Apparent apparent = layer2RelativeApparent(layer2Absolute);
        return new ApparentRecord(system,
            apparent.value() * layer2Absolute.rho1(), apparent.derivative() * layer2Absolute.rho1());
      }

      private Apparent layer3RelativeApparent(Model.Layer3Model layer3Relative) {
        DoubleUnaryOperator left = braceOperation(layer3Relative.hStep(), Sign.MINUS);
        DoubleUnaryOperator right = braceOperation(layer3Relative.hStep(), Sign.PLUS);
        double[] qn = Layers.qn(layer3Relative.k1().value(), layer3Relative.k2().value(), layer3Relative.p().p1(), layer3Relative.p().p2mp1());
        double apparent = (1.0 + 2.0 * Layers.sum(n -> qn[n] * (left.applyAsDouble(n) - right.applyAsDouble(n))));
        return new ApparentRecord(system, apparent, Double.NaN);
      }

      private Apparent layer3AbsoluteApparent(Model.Layer3Absolute layer3Absolute) {
        Apparent apparent = layer3RelativeApparent(layer3Absolute);
        return new ApparentRecord(system,
            apparent.value() * layer3Absolute.rho1(), apparent.derivative() * layer3Absolute.rho1());
      }
    }
  }
}
