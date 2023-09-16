package com.ak.digitalfilter;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

final class AngleFilter extends AbstractOperableFilter {
  private int y;

  @Override
  public int applyAsInt(int in) {
    if (checkResetAndClear()) {
      y = in;
    }

    Complex cOut = ComplexUtils.polar2Complex(1.0, Math.toRadians(y / 1000.0));
    Complex cIn = ComplexUtils.polar2Complex(1.0, Math.toRadians(in / 1000.0));

    int signum = (int) Math.signum(cOut.getReal() * cIn.getImaginary() - cOut.getImaginary() * cIn.getReal());
    y += ((in - y % 360000) + signum * 360000) % 360000;
    return y;
  }
}