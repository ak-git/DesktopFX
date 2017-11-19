package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
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

  public FilterBuilder operator(@Nonnull Provider<IntUnaryOperator> operatorProvider) {
    return chain(new AbstractOperableFilter() {
      @Nonnull
      private final IntUnaryOperator operator = operatorProvider.get();

      @Override
      public int applyAsInt(int in) {
        return operator.applyAsInt(in);
      }

      @Override
      public String toString() {
        return "Operator " + super.toString();
      }
    });
  }

  public FilterBuilder biOperator(@Nonnull Provider<IntBinaryOperator> operatorProvider) {
    return chain(new AbstractDigitalFilter() {
      @Nonnull
      private final IntBinaryOperator operator = operatorProvider.get();

      @Override
      public int getOutputDataSize() {
        return 1;
      }

      @Override
      public void accept(@Nonnull int... values) {
        Objects.requireNonNull(values);
        publish(operator.applyAsInt(values[0], values[1]));
      }

      @Override
      public String toString() {
        return "BiOperator " + super.toString();
      }
    });
  }

  public FilterBuilder fir(@Nonnull Provider<double[]> coefficients) {
    return fir(coefficients.get());
  }

  public FilterBuilder smoothingImpulsive(@Nonnegative int size) {
    HoldFilter holdFilter = new HoldFilter.Builder(size).lostCount(1);
    return chain(holdFilter).chain(new DecimationFilter(size)).operator(() -> operand -> {
      int[] sorted = holdFilter.getSorted();
      double mean = Arrays.stream(sorted).average().orElse(0.0);

      int posCount = 0;
      int negCount = 0;
      double distances = 0.0;
      for (int n : sorted) {
        if (n > mean) {
          posCount++;
          distances += (n - mean);
        }
        else if (n < mean) {
          negCount++;
        }
      }
      return (int) Math.round(mean + (posCount - negCount) * distances / StrictMath.pow(size, 2));
    }).interpolate(size);
  }

  public FilterBuilder sharpingDecimate(@Nonnegative int size) {
    HoldFilter holdFilter = new HoldFilter.Builder(size).lostCount(0);
    return chain(holdFilter).chain(new DecimationFilter(size)).operator(() -> new IntUnaryOperator() {
      private int prev;

      @Override
      public int applyAsInt(int operand) {
        int[] sorted = holdFilter.getSorted();
        int min = sorted[0];
        int max = sorted[sorted.length - 1];
        prev = Math.abs(prev - max) > Math.abs(prev - min) ? max : min;
        return prev;
      }
    });
  }

  public FilterBuilder expSum() {
    return chain(new ExpSumFilter());
  }

  FilterBuilder fir(double... coefficients) {
    return chain(new FIRFilter(coefficients));
  }

  public FilterBuilder comb(@Nonnegative int combFactor) {
    return chain(new CombFilter(combFactor));
  }

  FilterBuilder integrate() {
    return chain(new IntegrateFilter());
  }

  FilterBuilder rrs(@Nonnegative int averageFactor) {
    return wrap(String.format("RRS%d", averageFactor),
        of().comb(averageFactor).integrate().operator(() -> n -> n / averageFactor));
  }

  FilterBuilder decimate(@Nonnegative int decimateFactor) {
    int combFactor = Math.max(decimateFactor / 2, 1);
    return wrap("LinearDecimationFilter",
        of().integrate().chain(new DecimationFilter(decimateFactor)).comb(combFactor).
            operator(() -> n -> n / decimateFactor / combFactor));
  }

  FilterBuilder interpolate(@Nonnegative int interpolateFactor) {
    int combFactor = Math.max(interpolateFactor / 2, 1);
    return wrap("LinearInterpolationFilter",
        of().comb(combFactor).chain(new InterpolationFilter(interpolateFactor)).integrate().
            operator(() -> n -> n / interpolateFactor / combFactor));
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

  public int[] filter(@Nonnull int[] ints) {
    DigitalFilter filter = build();
    int[] result = new int[(int) Math.floor(ints.length * filter.getFrequencyFactor())];
    AtomicInteger index = new AtomicInteger();
    filter.forEach(values -> result[index.getAndIncrement()] = values[0]);
    for (int i : ints) {
      filter.accept(i);
    }
    return result;
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

  private FilterBuilder wrap(@Nonnull String name, @Nonnull Builder<DigitalFilter> filterBuilder) {
    return chain(new FilterWrapper(name, filterBuilder.build()));
  }
}
