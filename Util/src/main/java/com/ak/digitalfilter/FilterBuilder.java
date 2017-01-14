package com.ak.digitalfilter;

import java.util.Optional;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.util.Builder;

public class FilterBuilder implements Builder<DigitalFilter> {
  @Nullable
  private DigitalFilter filter;

  private FilterBuilder() {
  }

  public static FilterBuilder of() {
    return new FilterBuilder();
  }

  static FilterBuilder parallel(@Nonnull DigitalFilter... filters) {
    FilterBuilder filterBuilder = new FilterBuilder();
    filterBuilder.filter = new ForkFilter(filters, true);
    return filterBuilder;
  }

  FilterBuilder fir(double... koeff) {
    return chain(new FIRFilter(koeff));
  }

  FilterBuilder comb(@Nonnegative int combFactor) {
    return chain(new CombFilter(combFactor));
  }

  FilterBuilder integrate() {
    return chain(new IntegrateFilter());
  }

  FilterBuilder rrs(@Nonnegative int averageFactor) {
    return chain(new RecursiveRunningSumFilter(averageFactor));
  }

  FilterBuilder decimate(@Nonnegative int decimateFactor) {
    return chain(new LinearDecimationFilter(decimateFactor));
  }

  FilterBuilder interpolate(@Nonnegative int interpolateFactor) {
    return chain(new LinearInterpolationFilter(interpolateFactor));
  }

  FilterBuilder fork(@Nonnull DigitalFilter... filters) {
    return chain(new ForkFilter(filters, false));
  }

  DigitalFilter buildNoDelay() {
    return new NoDelayFilter(build());
  }

  @Override
  public DigitalFilter build() {
    return Optional.ofNullable(filter).orElse(new NoFilter());
  }

  private FilterBuilder chain(@Nonnull DigitalFilter chain) {
    filter = Optional.ofNullable(filter).<DigitalFilter>map(filter -> new ChainFilter(filter, chain)).orElse(chain);
    return this;
  }
}
