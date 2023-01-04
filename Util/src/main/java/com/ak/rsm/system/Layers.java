package com.ak.rsm.system;

import com.ak.numbers.CoefficientsUtils;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

public enum Layers {
  ;

  private static final int SUM_LIMIT = 1 << 15;
  private static final int SUM_LIMIT_STEP = 1 << 10;

  public static double getK12(@Nonnegative double rho1, @Nonnegative double rho2) {
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

  public static double getRho1ToRho2(double k12) {
    double k = Math.max(-1.0, Math.min(k12, 1.0));
    return (1.0 - k) / (1.0 + k);
  }

  public static double sum(@Nonnull IntToDoubleFunction function) {
    double sum = 0.0;
    for (int i = 0; i < SUM_LIMIT / SUM_LIMIT_STEP; i++) {
      double prev = sum;
      sum += IntStream.rangeClosed(SUM_LIMIT_STEP * i + 1, SUM_LIMIT_STEP * (i + 1))
          .unordered().parallel().mapToDouble(function).sum();
      if (Double.compare(prev, sum) == 0) {
        break;
      }
    }
    return sum;
  }

  public static double[] qn(double k12, double k23, @Nonnegative int p1, @Nonnegative int p2mp1) {
    int p2 = p2mp1 + p1;

    var bNum = new double[p2 + 1];
    bNum[p1] += k12;
    bNum[p2] += k23;

    var aDen = new double[p2 + 1];
    aDen[0] = 1;
    aDen[p1] -= k12;
    aDen[p2] -= k23;
    aDen[p2 - p1] += k12 * k23;

    double[] doubles = CoefficientsUtils.serialize(bNum, aDen, p1 + p2mp1 + 1);
    var q = new double[SUM_LIMIT + 1];
    System.arraycopy(doubles, 0, q, 0, doubles.length);
    for (int p2pm = doubles.length; p2pm < q.length; p2pm++) {
      q[p2pm] = k12 * q[p2pm - p1] + k23 * q[p2pm - p2] - k12 * k23 * q[p2pm - p2 + p1];
    }
    return q;
  }
}
