package com.ak.rsm2;

import java.util.Objects;

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
      this(h * (factor(system, -1) - factor(system, 1)));
    }

    private static double factor(ElectrodeSystem.Tetrapolar system, int sign) {
      return 1.0 / Math.abs(system.lCC() + sign * system.sPU());
    }
  }
}
