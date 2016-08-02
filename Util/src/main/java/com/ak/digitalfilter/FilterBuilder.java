package com.ak.digitalfilter;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import javafx.util.Builder;

class FilterBuilder implements Builder<DigitalFilter> {
  private DigitalFilter filter;

  private FilterBuilder() {
  }

  @Nonnull
  static FilterBuilder of() {
    return new FilterBuilder();
  }

  FilterBuilder fir(double... koeff) {
    filter = Optional.ofNullable(filter).<DigitalFilter>map(filter -> new ChainFilter(filter, new FIRFilter(koeff))).
        orElse(new FIRFilter(koeff));
    return this;
  }

  @Nonnull
  @Override
  public DigitalFilter build() {
    Objects.requireNonNull(filter);
    return filter;
  }
}
