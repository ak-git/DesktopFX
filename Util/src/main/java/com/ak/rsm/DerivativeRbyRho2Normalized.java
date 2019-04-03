package com.ak.rsm;

import javax.annotation.Nonnegative;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

/**
 * Calculates <b>first derivative</b> resistance R<sub>m-n</sub> (in Ohm) by <b>Rho-2</b> normalized by Rho-2 / R for 2-layer model.
 * <br/>
 * <b>dR<sub>Rho-2</sub> * (Rho-2 / R)</b>
 */
final class DerivativeRbyRho2Normalized extends AbstractDerivativeRNormalized {
  /**
   * Construct Derivative R by Rho2 Normalized by Rho2 / R
   *
   * @param k12  (rho2 - rho2) / (rho2 + rho1)
   * @param sToL relation s / L
   */
  DerivativeRbyRho2Normalized(double k12, double sToL) {
    super(k12, sToL, 1.0);
  }

  @Override
  double nominator(@Nonnegative double hSI) {
    return (1.0 - pow(k12(), 2.0)) *
        ResistanceTwoLayer.sum(hSI, n -> n * pow(k12(), n - 1), (qn, b) ->
            qn * (1.0 / hypot(electrodes().radiusMinus(), b) - 1.0 / hypot(electrodes().radiusPlus(), b)));
  }
}
