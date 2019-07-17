package com.ak.rsm;

import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnegative;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log1p;
import static java.lang.StrictMath.pow;

final class Log1pApparent3Rho extends AbstractLogApparent3Rho {
  Log1pApparent3Rho(@Nonnegative double sToL, @Nonnegative double Lh) {
    super(sToL, Lh);
  }

  @Override
  double innerValue(double sums) {
    double v = Lh() * (1.0 - pow(sToL(), 2.0));
    if (v > 0) {
      v /= sToL();
    }
    else {
      v = abs(v);
    }
    return log1p(v * sums);
  }

  @Override
  IntToDoubleFunction sum(double[] q) {
    return sign -> Layers.sum(n -> q[n], n -> hypot(Lh() * (1.0 + sign * sToL()), 4.0 * n));
  }
}
