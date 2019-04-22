package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.TrivariateFunction;

/**
 * Calculates <b>first derivative</b> resistance R<sub>m-n</sub> (in Ohm) by <b>h</b> for 2-layer model.
 * <br/>
 * <b>dR<sub>h</sub></b>
 */

final class DerivativeRbyH extends AbstractDerivativeR implements TrivariateFunction {
  DerivativeRbyH(@Nonnull TetrapolarSystem electrodes) {
    super(electrodes);
  }

  @Override
  public double value(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double hSI) {
    return 64 * rho1 * hSI * sumN2kN(ResistanceTwoLayer.getK12(rho1, rho2), hSI) / Math.PI;
  }
}
