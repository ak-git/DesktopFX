package com.ak.rsm2;

import com.ak.rsm.system.Layers;
import com.ak.util.Builder;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

public sealed interface Resistivity {
  ElectrodeSystem.Tetrapolar system();

  double apparent(double rOhm);

  static Step1 of(ElectrodeSystem.Tetrapolar system) {
    return new ResistivityBuilder(system);
  }

  sealed interface Step1 extends Builder<Resistivity> {
    Apparent apparentDivRho1(Model.Layer2Relative layer2Relative);

    Apparent apparentDivRho1(Model.Layer2RelativeDH layer2Relative);

    Apparent apparent(Model.Layer2Absolute layer2Absolute);
  }

  final class ResistivityBuilder implements Step1 {
    private record ResistivityRecord(ElectrodeSystem.Tetrapolar system) implements Resistivity {
      private ResistivityRecord {
        Objects.requireNonNull(system);
      }

      @Override
      public double apparent(double rOhm) {
        return (Math.PI / 2.0) * rOhm / system.phiFactor();
      }
    }

    private final ElectrodeSystem.Tetrapolar tetrapolar;

    private ResistivityBuilder(ElectrodeSystem.Tetrapolar tetrapolar) {
      this.tetrapolar = tetrapolar;
    }

    @Override
    public Resistivity build() {
      return new ResistivityRecord(tetrapolar);
    }

    @Override
    public Apparent apparentDivRho1(Model.Layer2Relative layer2Relative) {
      return new Apparent.ApparentBuilder(build(), layer2Relative).build();
    }

    @Override
    public Apparent apparentDivRho1(Model.Layer2RelativeDH layer2Relative) {
      return new Apparent.ApparentBuilder(build(), layer2Relative).build();
    }

    @Override
    public Apparent apparent(Model.Layer2Absolute layer2Absolute) {
      return new Apparent.ApparentBuilder(build(), layer2Absolute).build();
    }
  }

  sealed interface Apparent extends Resistivity {
    double value();

    double derivativeByPhi();

    final class ApparentBuilder implements Builder<Apparent> {
      private record ApparentRecord(Resistivity resistivity, double value, double derivativeByPhi)
          implements Apparent {
        private ApparentRecord {
          Objects.requireNonNull(resistivity);
        }

        @Override
        public ElectrodeSystem.Tetrapolar system() {
          return resistivity.system();
        }

        @Override
        public double apparent(double rOhm) {
          return resistivity.apparent(rOhm);
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

      private final Resistivity resistivity;
      private final Model model;

      private ApparentBuilder(Resistivity resistivity, Model model) {
        this.resistivity = resistivity;
        this.model = model;
      }

      @Override
      public Apparent build() {
        return switch (model) {
          case Model.Layer2Relative layer2Relative ->
              new ApparentRecord(resistivity, apparentDivRho1(layer2Relative), derivativeApparentByPhiDivRho1(layer2Relative));
          case Model.Layer2RelativeDH(K k, double h, _) -> {
            Model.Layer2Relative layer2Relative = new Model.Layer2Relative(k, h);
            yield new ApparentRecord(resistivity, apparentDivRho1(layer2Relative), derivativeApparentByPhiDivRho1(layer2Relative));
          }
          case Model.Layer2Absolute(double rho1, double rho2, double h, _) -> {
            Model.Layer2Relative layer2Relative = new Model.Layer2Relative(K.of(rho1, rho2), h);
            yield new ApparentRecord(resistivity, rho1 * apparentDivRho1(layer2Relative), rho1 * derivativeApparentByPhiDivRho1(layer2Relative));
          }
        };
      }

      private double apparentDivRho1(Model.Layer2Relative layer2) {
        DoubleUnaryOperator left = braceOperation(layer2.h(), Sign.MINUS);
        DoubleUnaryOperator right = braceOperation(layer2.h(), Sign.PLUS);
        return 1.0 + 2.0 * Layers.sum(n -> pow(layer2.k().value(), n) * (left.applyAsDouble(n) - right.applyAsDouble(n)));
      }

      private double derivativeApparentByPhiDivRho1(Model.Layer2Relative layer2) {
        DoubleUnaryOperator left = braceOperation(layer2.h(), Sign.MINUS);
        DoubleUnaryOperator right = braceOperation(layer2.h(), Sign.PLUS);
        return -32.0 * layer2.h() * resistivity.system().phiFactor() *
            Layers.sum(
                n -> pow(layer2.k().value(), n) * n * n * (pow(left.applyAsDouble(n), 3.0) - pow(right.applyAsDouble(n), 3.0))
            );
      }

      private DoubleUnaryOperator braceOperation(double hSI, DoubleUnaryOperator sign) {
        return n -> {
          double nom = 1.0 + sign.applyAsDouble(resistivity.system().sToL());
          double den = 1.0 + sign.andThen(Sign.MINUS).applyAsDouble(resistivity.system().sToL());
          double left = 1.0 - Math.abs(nom / den);
          double right = 4.0 * n * hSI * resistivity.system().phiFactor();
          return 1.0 / hypot(left, right);
        };
      }
    }
  }
}
