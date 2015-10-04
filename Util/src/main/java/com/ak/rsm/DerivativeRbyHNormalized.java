package com.ak.rsm;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

/**
 * Calculates <b>first derivative</b> resistance R<sub>m-n</sub> (in Ohm) by <b>h</b> normalized by L / R for 2-layer model.
 * <br/>
 * <b>dR<sub>h</sub> * (L / R)</b>
 */
final class DerivativeRbyHNormalized extends AbstractDerivativeRNormalized {
  /**
   * Construct Derivative R by h Normalized by L / R
   *
   * @param k12  (rho2 - rho2) / (rho2 + rho1)
   * @param sToL relation s / L
   */
  DerivativeRbyHNormalized(double k12, double sToL) {
    super(k12, sToL);
  }

  @Override
  double nominator(double hSI) {
    double sum = 0.0;
    for (int n = 1; ; n++) {
      double b = 4.0 * n * hSI;
      double prev = sum;
      sum += pow(n, 2.0) * pow(k12(), n) *
          (-1.0 / pow(hypot(electrodes().radiusMinus(), b), 3.0) + 1.0 / pow(hypot(electrodes().radiusPlus(), b), 3.0));
      if (Double.compare(prev, sum) == 0) {
        break;
      }
    }
    return 32 * sum * hSI;
  }
}
