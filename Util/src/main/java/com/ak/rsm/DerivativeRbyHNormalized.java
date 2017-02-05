package com.ak.rsm;

import javax.annotation.Nonnegative;

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
    super(k12, sToL, 1.0);
  }

  @Override
  double nominator(@Nonnegative double hSI) {
    return 32 * hSI * sumN2kN(hSI);
  }
}
