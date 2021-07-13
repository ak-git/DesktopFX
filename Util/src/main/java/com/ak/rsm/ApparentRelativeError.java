package com.ak.rsm;

import java.util.function.DoubleSupplier;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.pow;

final class ApparentRelativeError extends AbstractApparent implements DoubleSupplier {
  @Nonnegative
  private final double dLToL;

  ApparentRelativeError(@Nonnull TetrapolarSystem system) {
    super(system.toRelative());
    dLToL = system.getAbsError() / system.getL();
  }

  @Override
  public double getAsDouble() {
    return dLToL * electrodesFactor() / pow(factor(-1.0), 2.0);
  }
}
