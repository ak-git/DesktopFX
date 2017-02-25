package com.ak.digitalfilter;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import javafx.util.Builder;

public class FilterBuilder implements Builder<DigitalFilter> {
  @Nullable
  private DigitalFilter filter;

  private FilterBuilder() {
  }

  public static DigitalFilter parallel(@Nonnull DigitalFilter... filters) {
    Objects.requireNonNull(filters);
    FilterBuilder filterBuilder = new FilterBuilder();
    if (filters.length == 0) {
      throw new IllegalArgumentException();
    }
    else if (filters.length == 1) {
      filterBuilder.filter = filters[0];
    }
    else {
      filterBuilder.filter = new ForkFilter(filters, true);
    }
    return filterBuilder.buildNoDelay();
  }

  public static FilterBuilder of() {
    return new FilterBuilder();
  }

  FilterBuilder fir(Provider<double[]> coefficients) {
    return fir(coefficients.get());
  }

  FilterBuilder fir(double... coefficients) {
    return chain(new FIRFilter(coefficients));
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
    Objects.requireNonNull(filters);
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
