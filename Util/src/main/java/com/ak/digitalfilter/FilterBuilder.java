package com.ak.digitalfilter;

import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nonnull;

import javafx.util.Builder;

class FilterBuilder implements Builder<DoubleUnaryOperator> {
  private DoubleUnaryOperator filter = DoubleUnaryOperator.identity();

  private FilterBuilder() {
  }

  @Nonnull
  static FilterBuilder of() {
    return new FilterBuilder();
  }

  FilterBuilder fir(double... koeff) {
    filter = filter.andThen(new FIRFilter(koeff));
    return this;
  }

  @Nonnull
  @Override
  public DoubleUnaryOperator build() {
    return filter;
  }
}
