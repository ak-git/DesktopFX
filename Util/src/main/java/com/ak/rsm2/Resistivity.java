package com.ak.rsm2;

import com.ak.rsm.system.Layers;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

public sealed interface Resistivity {
  double apparent(double rOhm);

  double apparentDivRho1(Model.Layer2Relative layer2);

  double derivativeApparentByPhiDivRho1(Model.Layer2Relative layer2);

  static Resistivity of(ElectrodeSystem.Tetrapolar tetrapolar) {
    return new ApparentDivRho1.TwoLayers(tetrapolar);
  }

  interface ApparentDivRho1 {
    record TwoLayers(ElectrodeSystem.Tetrapolar tetrapolar) implements Resistivity {
      enum Sign implements DoubleUnaryOperator {
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

      public TwoLayers {
        Objects.requireNonNull(tetrapolar);
      }

      @Override
      public double apparent(double rOhm) {
        return (Math.PI / 2.0) * rOhm / tetrapolar().phiFactor();
      }

      @Override
      public double apparentDivRho1(Model.Layer2Relative layer2) {
        DoubleUnaryOperator left = braceOperation(layer2.hSI(), Sign.MINUS);
        DoubleUnaryOperator right = braceOperation(layer2.hSI(), Sign.PLUS);
        return 1.0 + 2.0 * Layers.sum(n -> pow(layer2.k(), n) * (left.applyAsDouble(n) - right.applyAsDouble(n)));
      }

      @Override
      public double derivativeApparentByPhiDivRho1(Model.Layer2Relative layer2) {
        DoubleUnaryOperator left = braceOperation(layer2.hSI(), Sign.MINUS);
        DoubleUnaryOperator right = braceOperation(layer2.hSI(), Sign.PLUS);
        return -32.0 * layer2.hSI() * tetrapolar.phiFactor() *
            Layers.sum(
                n -> pow(layer2.k(), n) * n * n * (pow(left.applyAsDouble(n), 3.0) - pow(right.applyAsDouble(n), 3.0))
            );
      }

      private DoubleUnaryOperator braceOperation(double hSI, DoubleUnaryOperator sign) {
        return n -> {
          double nom = 1.0 + sign.applyAsDouble(tetrapolar.sToL());
          double den = 1.0 + sign.andThen(Sign.MINUS).applyAsDouble(tetrapolar.sToL());
          double left = 1.0 - Math.abs(nom / den);
          double right = 4.0 * n * hSI * tetrapolar.phiFactor();
          return 1.0 / hypot(left, right);
        };
      }
    }
  }
}
