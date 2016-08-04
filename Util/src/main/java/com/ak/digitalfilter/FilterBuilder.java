package com.ak.digitalfilter;

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

  @Nonnull
  FilterBuilder fir(double... koeff) {
    return chain(new FIRFilter(koeff));
  }

  @Nonnull
  FilterBuilder fork(@Nonnull DigitalFilter first, @Nonnull DigitalFilter... next) {
    filter = new ChainFilter(build(), new ForkFilter(first, next));
    return this;
  }

  @Nonnull
  DigitalFilter buildNoDelay() {
    return new NoDelayFilter(build());
  }

  @Nonnull
  @Override
  public DigitalFilter build() {
    return Optional.ofNullable(filter).orElse(new NoFilter());
  }

  @Nonnull
  private FilterBuilder chain(@Nonnull DigitalFilter chain) {
    filter = Optional.ofNullable(filter).<DigitalFilter>map(filter -> new ChainFilter(filter, chain)).orElse(chain);
    return this;
  }
}
