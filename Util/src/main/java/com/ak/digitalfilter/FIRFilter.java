package com.ak.digitalfilter;

import java.util.Arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class FIRFilter extends OperableFilter {
  private final int[] buffer;
  private final double[] koeff;
  private int bufferIndex = -1;

  FIRFilter(@Nonnull double[] koeff) {
    this.koeff = Arrays.copyOf(koeff, koeff.length);
    buffer = new int[koeff.length];
  }

  @Nonnegative
  @Override
  public double getDelay() {
    return (buffer.length - 1) / 2.0;
  }

  @Override
  public int applyAsInt(int in) {
    bufferIndex = (++bufferIndex) % buffer.length;
    buffer[bufferIndex] = in;

    double result = 0;
    for (int i = 0; i < koeff.length; i++) {
      result += buffer[(1 + i + bufferIndex) % buffer.length] * koeff[i];
    }
    return (int) Math.round(result);
  }

}