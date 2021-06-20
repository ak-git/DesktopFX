package com.ak.rsm;

import java.util.function.DoubleSupplier;

import javax.annotation.Nonnull;

import static java.lang.StrictMath.pow;

final class ApparentAbsError extends AbstractApparent implements DoubleSupplier {
  @Nonnull
  private final Measurement measurement;

  ApparentAbsError(@Nonnull Measurement measurement) {
    super(measurement.getSystem().toRelative());
    this.measurement = measurement;
  }

  @Override
  public double getAsDouble() {
    double dL = measurement.getSystem().getAbsError();
    double ohmsR = measurement.getR();
    return Math.PI * ohmsR * dL * pow(electrodesFactor(), 2.0) / (4.0 * pow(factor(-1.0), 2.0));
  }
}
