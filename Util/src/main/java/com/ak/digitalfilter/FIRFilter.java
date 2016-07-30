package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

final class FIRFilter implements DigitalFilter, DoubleUnaryOperator {
  private final double[] buffer;
  private final double[] koeff;
  private int bufferIndex = -1;

  FIRFilter(double[] koeff) {
    this.koeff = Arrays.copyOf(koeff, koeff.length);
    buffer = new double[koeff.length];
  }

  @Override
  public double applyAsDouble(double in) {
    bufferIndex = (++bufferIndex) % buffer.length;
    buffer[bufferIndex] = in;

    double result = 0;
    for (int i = 0; i < koeff.length; i++) {
      result += buffer[(1 + i + bufferIndex) % buffer.length] * koeff[i];
    }
    return result;
  }

  @Override
  public double delay() {
    return (buffer.length - 1) / 2.0;
  }
}