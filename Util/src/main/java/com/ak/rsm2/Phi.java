package com.ak.rsm2;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

public sealed interface Phi extends DoubleValuable {
  static Phi of(double h, ElectrodeSystem.Tetrapolar system) {
    return new PhiRecord(h, system);
  }

  record PhiRecord(double value) implements Phi {
    public PhiRecord(double h, ElectrodeSystem.Tetrapolar system) {
      if (h < 0) {
        throw new IllegalArgumentException("h = %f must be non-negative".formatted(h));
      }
      Objects.requireNonNull(system);
      this(h * (factor(system, Sign.MINUS) - factor(system, Sign.PLUS)));
    }

    private static double factor(ElectrodeSystem.Tetrapolar system, DoubleUnaryOperator sign) {
      return 1.0 / Math.abs(system.lCC() + sign.applyAsDouble(system.sPU()));
    }
  }
}
