package com.ak.rsm2;

import com.ak.rsm.system.Layers;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

public sealed interface Resistivity {
  double apparent(double rOhm);

  ApparentDivRho1 apparentDivRho1();

  ApparentDivRho1 derivativeApparentByPhoDivRho1();

  static Resistivity of(ElectrodeSystem.Tetrapolar tetrapolar) {
    return new ApparentDivRho1.TwoLayers(tetrapolar);
  }

  interface ApparentDivRho1 {
    double value(double k, double hSI);

    record TwoLayers(ElectrodeSystem.Tetrapolar tetrapolar) implements Resistivity {
      public TwoLayers {
        Objects.requireNonNull(tetrapolar);
      }

      @Override
      public double apparent(double rOhm) {
        return (Math.PI / 2.0) * rOhm / tetrapolar().phiFactor();
      }

      @Override
      public ApparentDivRho1 apparentDivRho1() {
        return (k, hSI) -> {
          DoubleUnaryOperator left = braceOperation(hSI, Sign.MINUS);
          DoubleUnaryOperator right = braceOperation(hSI, Sign.PLUS);
          return 1.0 + 2.0 * Layers.sum(n -> pow(k, n) * (left.applyAsDouble(n) - right.applyAsDouble(n)));
        };
      }

      @Override
      public ApparentDivRho1 derivativeApparentByPhoDivRho1() {
        return (k, hSI) -> {
          DoubleUnaryOperator left = braceOperation(hSI, Sign.MINUS);
          DoubleUnaryOperator right = braceOperation(hSI, Sign.PLUS);
          return -32.0 * hSI * tetrapolar.phiFactor() *
              Layers.sum(
                  n -> pow(k, n) * n * n * (pow(left.applyAsDouble(n), 3.0) - pow(right.applyAsDouble(n), 3.0))
              );
        };
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
