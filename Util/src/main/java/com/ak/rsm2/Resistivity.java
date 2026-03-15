package com.ak.rsm2;

import com.ak.rsm.system.Layers;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

enum Resistivity {
  ;

  static NormalizedByRho1 of(ElectrodeSystem relative) {
    return new NormalizedByRho1.NormalizedByRho1TwoLayers(relative);
  }

  sealed interface NormalizedByRho1 {
    double value(K k, Phi phi);

    record NormalizedByRho1TwoLayers(ElectrodeSystem relative) implements NormalizedByRho1 {
      public NormalizedByRho1TwoLayers {
        Objects.requireNonNull(relative);
      }

      @Override
      public double value(K k, Phi phi) {
        DoubleUnaryOperator left = braceOperation(phi, Sign.MINUS);
        DoubleUnaryOperator right = braceOperation(phi, Sign.PLUS);
        return 1.0 + 2.0 * Layers.sum(n -> StrictMath.pow(k.value(), n) * (left.applyAsDouble(n) - right.applyAsDouble(n)));
      }

      private DoubleUnaryOperator braceOperation(DoubleValuable phi, DoubleUnaryOperator sign) {
        return n -> {
          double nom = 1.0 + sign.applyAsDouble(relative.sToL());
          double den = 1.0 + sign.andThen(Sign.MINUS).applyAsDouble(relative.sToL());
          double left = 1.0 - Math.abs(nom / den);
          double right = 4.0 * n * phi.value();
          return 1.0 / StrictMath.hypot(left, right);
        };
      }
    }
  }
}
