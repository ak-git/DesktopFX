package com.ak.rsm;

import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.UnivariateFunction;
import tec.uom.se.unit.Units;

abstract class AbstractDerivativeRNormalized implements UnivariateFunction, Cloneable {
  @Nonnull
  private final TetrapolarSystem electrodes;
  private final double k12;

  AbstractDerivativeRNormalized(double k12, double sToL) {
    electrodes = new TetrapolarSystem(sToL, 1.0, Units.METRE);
    this.k12 = k12;
  }

  @Override
  public final double value(double hToL) {
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

  @Override
  protected final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
