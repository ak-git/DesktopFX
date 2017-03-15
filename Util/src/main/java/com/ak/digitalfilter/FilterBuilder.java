package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import javafx.util.Builder;

public class FilterBuilder implements Builder<DigitalFilter> {
  private static final int[] EMPTY_INTS = {};
  @Nullable
  private DigitalFilter filter;

  private FilterBuilder() {
  }

  public static DigitalFilter parallel(@Nonnull List<int[]> selectedIndexes, @Nonnull DigitalFilter... filters) {
    if (selectedIndexes.isEmpty()) {
      throw new IllegalArgumentException(Arrays.deepToString(filters));
    }
    return of().fork(selectedIndexes, filters).buildNoDelay();
  }

  static DigitalFilter parallel(@Nonnull DigitalFilter... filters) {
    Objects.requireNonNull(filters);
    return parallel(Stream.generate(() -> EMPTY_INTS).limit(filters.length).collect(Collectors.toList()), filters);
  }

  public static FilterBuilder of() {
    return new FilterBuilder();
  }

  public FilterBuilder operator(@Nonnull IntUnaryOperator operator) {
    return chain(new AbstractOperableFilter() {
      @Override
      public int applyAsInt(int in) {
        return operator.applyAsInt(in);
      }
    });
  }

  public FilterBuilder function(@Nonnull ToIntFunction<int[]> function) {
    return chain(new AbstractDigitalFilter() {
      @Override
      public int size() {
        return 1;
      }

      @Override
      public void accept(@Nonnull int... values) {
        publish(function.applyAsInt(values));
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
    return fork(Collections.emptyList(), filters);
  }

  DigitalFilter buildNoDelay() {
    return new NoDelayFilter(build());
  }

  @Override
  public DigitalFilter build() {
    return Optional.ofNullable(filter).orElse(new NoFilter());
  }

  private FilterBuilder fork(@Nonnull List<int[]> selectedIndexes, @Nonnull DigitalFilter... filters) {
    Objects.requireNonNull(selectedIndexes);
    Objects.requireNonNull(filters);
    if (filters.length == 0) {
      throw new IllegalArgumentException();
    }
    DigitalFilter[] wrappedFilters;
    if (selectedIndexes.isEmpty()) {
      wrappedFilters = Arrays.copyOf(filters, filters.length);
    }
    else {
      if (selectedIndexes.size() != filters.length) {
        throw new IllegalArgumentException(String.format("selectedIndexes.length [%s] != filters.length [%s]",
            selectedIndexes.stream().map(Arrays::toString).collect(Collectors.joining()), Arrays.toString(filters)));
      }
      wrappedFilters = new DigitalFilter[filters.length];
      for (int i = 0; i < wrappedFilters.length; i++) {
        int[] ints = selectedIndexes.get(i);
        if (ints.length == 0) {
          ints = new int[] {i};
        }
        wrappedFilters[i] = new SelectFilter(ints, filters[i]);
      }
    }
    return filters.length == 1 ? chain(wrappedFilters[0]) : chain(new ForkFilter(wrappedFilters));
  }

  private FilterBuilder chain(@Nonnull DigitalFilter chain) {
    filter = Optional.ofNullable(filter).<DigitalFilter>map(filter -> new ChainFilter(filter, chain)).orElse(chain);
    return this;
  }
}
