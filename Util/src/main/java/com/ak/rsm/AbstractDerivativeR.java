package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

abstract class AbstractDerivativeR implements Cloneable {
  @Nonnull
  private final TetrapolarSystem electrodes;

  AbstractDerivativeR(@Nonnull TetrapolarSystem electrodes) {
    this.electrodes = electrodes;
  }

  final double sumN2kN(double k12, @Nonnegative double hSI) {
    return ResistanceTwoLayer.sum(hSI, n -> pow(n, 2.0) * pow(k12, n), (qn, b) -> qn *
        (-1.0 / pow(hypot(electrodes.radiusMinus(), b), 3.0) + 1.0 / pow(hypot(electrodes.radiusPlus(), b), 3.0)));
  }

  @Override
  protected final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
