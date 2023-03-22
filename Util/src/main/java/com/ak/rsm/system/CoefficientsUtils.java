package com.ak.rsm.system;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

enum CoefficientsUtils {
  ;

  public static double[] serialize(@Nonnull double[] bNum, @Nonnull double[] aDen, @Nonnegative int outLength) {
    var out = new double[outLength];
    out[0] = bNum[0] / aDen[0];
    for (var n = 1; n < out.length; n++) {
      var sum = 0.0;
      for (int i = 1, k = Math.min(aDen.length - 1, n); i <= k; i++) {
        sum += out[n - i] * aDen[i];
      }
      double bn = n < bNum.length ? bNum[n] : 0;
      out[n] = (bn - sum) / aDen[0];
    }
    return out;
  }
}
