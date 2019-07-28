package com.ak.rsm;

import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.numbers.CoefficientsUtils;

import static java.lang.StrictMath.hypot;

class Layers {
  private static final int SUM_LIMIT = 1024 * 512;

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

  static double getRho1ToRho2(double k) {
    k = Math.max(-1.0, Math.min(k, 1.0));
    if (Double.compare(k, -1.0) == 0) {
      return Double.POSITIVE_INFINITY;
    }
    return (1.0 - k) / (1.0 + k);
  }

  static double sum(@Nonnull IntToDoubleFunction nominator, @Nonnull IntToDoubleFunction denominator) {
    return IntStream.rangeClosed(1, SUM_LIMIT).unordered().parallel()
        .mapToDouble(n -> nominator.applyAsDouble(n) / denominator.applyAsDouble(n)).sum();
  }

  static IntToDoubleFunction denominator(@Nonnegative double r, @Nonnegative double h) {
    return n -> hypot(r, 2.0 * n * h);
  }

  static double[] qn(double k12, double k23, @Nonnegative int p1, @Nonnegative int p2mp1) {
    if (p1 < 1) {
      throw new IllegalArgumentException(String.format("p1 = %d < 0", p1));
    }
    int p2 = p2mp1 + p1;

    double[] bNum = new double[p2 + 1];
    bNum[p1] += k12;
    bNum[p2] += k23;

    double[] aDen = new double[p2 + 1];
    aDen[0] = 1;
    aDen[p1] -= k12;
    aDen[p2] -= k23;
    aDen[p2 - p1] += k12 * k23;

    return CoefficientsUtils.serialize(bNum, aDen, SUM_LIMIT + 1);
  }
}
