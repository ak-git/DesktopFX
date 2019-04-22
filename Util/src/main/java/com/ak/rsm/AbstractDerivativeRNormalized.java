package com.ak.rsm;

import javax.annotation.Nonnegative;

import org.apache.commons.math3.analysis.UnivariateFunction;
import tec.uom.se.unit.Units;

abstract class AbstractDerivativeRNormalized extends AbstractDerivativeR implements UnivariateFunction {
  private final double k12;

  AbstractDerivativeRNormalized(double k12, double sMetre, double lMetre) {
    super(new TetrapolarSystem(sMetre, lMetre, Units.METRE));
    this.k12 = k12;
  }

  @Override
  public double value(double hToL) {
    ResistanceTwoLayer rTwoLayer = new ResistanceTwoLayer(electrodes());
    double denominator = 1.0 / electrodes().radiusMinus() - 1.0 / electrodes().radiusPlus() + 2.0 * rTwoLayer.sum(k12, hToL);
    return nominator(hToL) / denominator;
  }

  abstract double nominator(double hToL);

  final double k12() {
    return k12;
  }

  final double sumN2kN(@Nonnegative double hSI) {
    return sumN2kN(k12, hSI);
  }
}
