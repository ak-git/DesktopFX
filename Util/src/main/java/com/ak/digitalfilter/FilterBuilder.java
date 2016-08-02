package com.ak.digitalfilter;

import javax.annotation.Nonnull;

import javafx.util.Builder;

class FilterBuilder implements Builder<DigitalFilter> {
  @Nonnull
  private DigitalFilter filter = new NoFilter();

  private FilterBuilder() {
  }

  @Nonnull
  static FilterBuilder of() {
    return new FilterBuilder();
  }

  FilterBuilder fir(double... koeff) {
    filter = new ChainFilter(filter, new FIRFilter(koeff));
    return this;
  }

  @Nonnull
  @Override
  public DigitalFilter build() {
    return filter;
  }
}
