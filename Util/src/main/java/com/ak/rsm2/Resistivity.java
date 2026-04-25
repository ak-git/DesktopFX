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
    ApparentDivRho1 apparentDivRho1(Model.Layer2Relative layer2Relative);
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
    public ApparentDivRho1 apparentDivRho1(Model.Layer2Relative layer2Relative) {
      return new ApparentDivRho1.ApparentDivRho1Builder(build(), layer2Relative).build();
    }
  }

  sealed interface ApparentDivRho1 extends Resistivity {
    double value();

    double derivativeByPhi();

    final class ApparentDivRho1Builder implements Builder<ApparentDivRho1> {
      private record ApparentDivRho1Record(Resistivity resistivity, double value, double derivativeByPhi)
          implements ApparentDivRho1 {
        private ApparentDivRho1Record {
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
      private final Model.Layer2Relative layer2Relative;

      private ApparentDivRho1Builder(Resistivity resistivity, Model.Layer2Relative layer2Relative) {
        this.resistivity = resistivity;
        this.layer2Relative = layer2Relative;
      }

      @Override
      public ApparentDivRho1 build() {
        return new ApparentDivRho1Record(resistivity, apparentDivRho1(layer2Relative), derivativeApparentByPhiDivRho1(layer2Relative));
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
