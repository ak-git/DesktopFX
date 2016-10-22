package com.ak.digitalfilter;

import java.util.Arrays;

import javax.annotation.Nonnull;

final class FIRFilter extends AbstractBufferFilter {
  @Nonnull
  private final double[] koeff;

  FIRFilter(@Nonnull double[] koeff) {
    super(koeff.length);
    this.koeff = Arrays.copyOf(koeff, koeff.length);
  }

  @Override
  int apply(int nowIndex) {
    double result = 0;
    for (int i = 0; i < koeff.length; i++) {
      result += get(1 + i + nowIndex) * koeff[i];
    }
    return (int) Math.round(result);
  }
}