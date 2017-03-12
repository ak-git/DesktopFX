package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import javafx.util.Builder;

public class FilterBuilder implements Builder<DigitalFilter> {
  private static final int[][] EMPTY_SELECTED_INDEXES = {};
  @Nullable
  private DigitalFilter filter;

  private FilterBuilder() {
  }

  public static DigitalFilter parallel(@Nonnull DigitalFilter... filters) {
    Objects.requireNonNull(filters);
    return of().fork(IntStream.range(0, filters.length).mapToObj(i -> new int[] {i}).toArray(value -> new int[value][1]), filters).buildNoDelay();
  }

  public static FilterBuilder of() {
    return new FilterBuilder();
  }

  public FilterBuilder function(@Nonnull IntUnaryOperator operator) {
    return chain(new AbstractOperableFilter() {
      @Override
      public int applyAsInt(int in) {
        return operator.applyAsInt(in);
      }
    });
  }

  public FilterBuilder fir(@Nonnull Provider<double[]> coefficients) {
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
    return fork(EMPTY_SELECTED_INDEXES, filters);
  }

  DigitalFilter buildNoDelay() {
    return new NoDelayFilter(build());
  }

  @Override
  public DigitalFilter build() {
    return Optional.ofNullable(filter).orElse(new NoFilter());
  }

  private FilterBuilder fork(@Nonnull int[][] selectedIndexes, @Nonnull DigitalFilter... filters) {
    Objects.requireNonNull(filters);
    Objects.requireNonNull(selectedIndexes);
    if (filters.length == 0) {
      throw new IllegalArgumentException();
    }
    DigitalFilter[] wrappedFilters;
    if (selectedIndexes.length == 0) {
      wrappedFilters = Arrays.copyOf(filters, filters.length);
    }
    else {
      if (selectedIndexes.length != filters.length) {
        throw new IllegalArgumentException(String.format("selectedIndexes.length [%s] != filters.length [%s]",
            Arrays.deepToString(selectedIndexes), Arrays.toString(filters)));
      }
      wrappedFilters = new DigitalFilter[filters.length];
      for (int i = 0; i < wrappedFilters.length; i++) {
        wrappedFilters[i] = new SelectFilter(selectedIndexes[i], filters[i]);
      }
    }
    return filters.length == 1 ? chain(wrappedFilters[0]) : chain(new ForkFilter(wrappedFilters));
  }

  private FilterBuilder chain(@Nonnull DigitalFilter chain) {
    filter = Optional.ofNullable(filter).<DigitalFilter>map(filter -> new ChainFilter(filter, chain)).orElse(chain);
    return this;
  }
}
