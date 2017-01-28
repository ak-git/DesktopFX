package com.ak.rsm;

import javax.annotation.Nonnegative;

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
    super(k12, sToL, 1.0);
  }

  @Override
  double nominator(@Nonnegative double hSI) {
    return 32 * hSI *
        ResistanceTwoLayer.sum(hSI, (n, b) -> pow(n, 2.0) * pow(k12(), n) *
            (-1.0 / pow(hypot(electrodes().radiusMinus(), b), 3.0) + 1.0 / pow(hypot(electrodes().radiusPlus(), b), 3.0)));
  }
}
