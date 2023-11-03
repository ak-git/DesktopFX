package com.ak.digitalfilter;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

final class AngleFilter extends AbstractOperableFilter {
  private static final double MILLI_DEG = 1000.0;
  private static final int FULL_ANGLE = 360_000;
  private int y;

  @Override
  public int applyAsInt(int in) {
    if (checkResetAndClear()) {
      y = in;
    }

    Complex cOut = ComplexUtils.polar2Complex(1.0, Math.toRadians(y / MILLI_DEG));
    Complex cIn = ComplexUtils.polar2Complex(1.0, Math.toRadians(in / MILLI_DEG));

    int signum = (int) Math.signum(cOut.getReal() * cIn.getImaginary() - cOut.getImaginary() * cIn.getReal());
    y += ((in - y % FULL_ANGLE) + signum * FULL_ANGLE) % FULL_ANGLE;
    return y;
  }
}