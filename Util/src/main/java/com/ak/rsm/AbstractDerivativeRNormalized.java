package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.UnivariateFunction;
import tec.uom.se.unit.Units;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

abstract class AbstractDerivativeRNormalized implements UnivariateFunction, Cloneable {
  @Nonnull
  private final TetrapolarSystem electrodes;
  private final double k12;

  AbstractDerivativeRNormalized(double k12, double sMetre, double lMetre) {
    electrodes = new TetrapolarSystem(sMetre, lMetre, Units.METRE);
    this.k12 = k12;
  }

  @Override
  public double value(double hToL) {
    ResistanceTwoLayer rTwoLayer = new ResistanceTwoLayer(electrodes);
    double denominator = 1.0 / electrodes.radiusMinus() - 1.0 / electrodes.radiusPlus() + 2.0 * rTwoLayer.sum(k12, hToL);
    return nominator(hToL) / denominator;
  }

  abstract double nominator(double hToL);

  final TetrapolarSystem electrodes() {
    return electrodes;
  }

  final double k12() {
    return k12;
  }

  final double sumN2kN(@Nonnegative double hSI) {
    return ResistanceTwoLayer.sum(hSI, n -> pow(n, 2.0) * pow(k12(), n), (qn, b) -> qn *
        (-1.0 / pow(hypot(electrodes().radiusMinus(), b), 3.0) + 1.0 / pow(hypot(electrodes().radiusPlus(), b), 3.0)));
  }

  @Override
  protected final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
