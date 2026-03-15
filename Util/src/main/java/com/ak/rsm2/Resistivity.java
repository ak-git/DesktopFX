package com.ak.rsm2;

import com.ak.rsm.system.Layers;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

enum Resistivity {
  ;

  static double apparent(ElectrodeSystem.Tetrapolar tetrapolar, double rOhm) {
    return rOhm / tetrapolar.phi(2.0 / Math.PI);
  }

  static NormalizedByRho1 normalizedByRho1(ElectrodeSystem.Tetrapolar tetrapolar) {
    return new NormalizedByRho1.NormalizedByRho1TwoLayers(tetrapolar);
  }

  sealed interface NormalizedByRho1 {
    double value(double k, double hSI);

    record NormalizedByRho1TwoLayers(ElectrodeSystem.Tetrapolar tetrapolar) implements NormalizedByRho1 {
      public NormalizedByRho1TwoLayers {
        Objects.requireNonNull(tetrapolar);
      }

      @Override
      public double value(double k, double hSI) {
        DoubleUnaryOperator left = braceOperation(hSI, Sign.MINUS);
        DoubleUnaryOperator right = braceOperation(hSI, Sign.PLUS);
        return 1.0 + 2.0 * Layers.sum(n -> StrictMath.pow(k, n) * (left.applyAsDouble(n) - right.applyAsDouble(n)));
      }

      private DoubleUnaryOperator braceOperation(double hSI, DoubleUnaryOperator sign) {
        return n -> {
          double nom = 1.0 + sign.applyAsDouble(tetrapolar().sToL());
          double den = 1.0 + sign.andThen(Sign.MINUS).applyAsDouble(tetrapolar().sToL());
          double left = 1.0 - Math.abs(nom / den);
          double right = 4.0 * n * tetrapolar().phi(hSI);
          return 1.0 / StrictMath.hypot(left, right);
        };
      }
    }
  }
}
