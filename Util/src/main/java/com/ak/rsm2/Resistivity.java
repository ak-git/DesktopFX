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
    Apparent apparentDivRho1(Model model);
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
    public Apparent apparentDivRho1(Model model) {
      return new Apparent.ApparentBuilder(build(), model).build();
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
          case Model.Layer2Relative(K k, double h) -> {
            DoubleUnaryOperator left = braceOperation(h, Sign.MINUS);
            DoubleUnaryOperator right = braceOperation(h, Sign.PLUS);
            yield new ApparentRecord(resistivity,
                1.0 + 2.0 * Layers.sum(n -> pow(k.value(), n) * (left.applyAsDouble(n) - right.applyAsDouble(n))),
                -32.0 * h * resistivity.system().phiFactor() *
                    Layers.sum(n -> pow(k.value(), n) * n * n * (pow(left.applyAsDouble(n), 3.0) - pow(right.applyAsDouble(n), 3.0)))
            );
          }
          case Model.Layer3Relative(K k12, K k23, double hStep, int p1, int p2mp1) -> {
            DoubleUnaryOperator left = braceOperation(hStep, Sign.MINUS);
            DoubleUnaryOperator right = braceOperation(hStep, Sign.PLUS);
            double[] qn = Layers.qn(k12.value(), k23.value(), p1, p2mp1);
            yield new ApparentRecord(resistivity,
                1.0 + 2.0 * Layers.sum(n -> qn[n] * (left.applyAsDouble(n) - right.applyAsDouble(n))),
                -32.0 * hStep * resistivity.system().phiFactor() *
                    Layers.sum(n -> qn[n] * n * n * (pow(left.applyAsDouble(n), 3.0) - pow(right.applyAsDouble(n), 3.0)))
            );
          }
        };
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
