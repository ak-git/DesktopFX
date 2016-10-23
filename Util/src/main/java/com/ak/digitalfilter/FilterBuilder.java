package com.ak.digitalfilter;

import java.util.Optional;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.util.Builder;

class FilterBuilder implements Builder<DigitalFilter> {
  @Nullable
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
  FilterBuilder comb(@Nonnegative int combFactor) {
    return chain(new CombFilter(combFactor));
  }

  @Nonnull
  FilterBuilder integrate() {
    return chain(new IntegrateFilter());
  }

  @Nonnull
  FilterBuilder rrs(@Nonnegative int averageFactor) {
    return chain(new RecursiveRunningSumFilter(averageFactor));
  }

  @Nonnull
  FilterBuilder decimate(@Nonnegative int decimateFactor) {
    return chain(new LinearDecimationFilter(decimateFactor));
  }

  @Nonnull
  FilterBuilder interpolate(@Nonnegative int interpolateFactor) {
    return chain(new LinearInterpolationFilter(interpolateFactor));
  }

  @Nonnull
  FilterBuilder fork(@Nonnull DigitalFilter first, @Nonnull DigitalFilter... next) {
    return chain(new ForkFilter(first, next));
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
