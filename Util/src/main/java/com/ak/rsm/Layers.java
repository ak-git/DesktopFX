package com.ak.rsm;

import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;

class Layers {
  static final int SUM_LIMIT = 1024 * 8;

  private Layers() {
  }

  static double getK12(@Nonnegative double rho1, @Nonnegative double rho2) {
    if (Double.compare(rho1, rho2) == 0) {
      return 0.0;
    }
    else if (Double.isInfinite(rho2)) {
      return 1.0;
    }
    else if (Double.isInfinite(rho1)) {
      return -1.0;
    }
    else {
      return (rho2 - rho1) / (rho2 + rho1);
    }
  }

  static double sum(@Nonnull IntToDoubleFunction nominator, @Nonnull IntToDoubleFunction denominator) {
    return IntStream.rangeClosed(1, SUM_LIMIT).unordered().parallel()
        .mapToDouble(n -> nominator.applyAsDouble(n) / denominator.applyAsDouble(n)).sum();
  }

  static IntToDoubleFunction denominator(@Nonnegative double r, @Nonnegative double h) {
    return n -> hypot(r, 2.0 * n * h);
  }
}
