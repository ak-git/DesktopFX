package com.ak.rsm;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

/**
 * Calculates <b>first derivative</b> resistance R<sub>m-n</sub> (in Ohm) by <b>h</b> normalized by rho<sub>1</sub> for 2-layer model.
 * <br/>
 * <b>dR<sub>h</sub> / rho<sub>1</sub></b>
 * <br/>
 * Rho<sub>1</sub> equals to 1 Ohm-m
 * <br/>
 * Rho<sub>2</sub> equals to Infinity
 */
final class DerivativeRbyHDivideByRho extends AbstractDerivativeRNormalized {
  DerivativeRbyHDivideByRho(double k12, double sMetre, double lMetre) {
    super(k12, sMetre, lMetre);
  }

  @Override
  public double value(double hSI) {
    return 64 * hSI *
        ResistanceTwoLayer.sum(hSI, (n, b) -> pow(n, 2.0) * pow(k12(), n) *
            (-1.0 / pow(hypot(electrodes().radiusMinus(), b), 3.0) + 1.0 / pow(hypot(electrodes().radiusPlus(), b), 3.0))) / Math.PI;
  }

  @Override
  double nominator(double hToL) {
    throw new UnsupportedOperationException();
  }
}
