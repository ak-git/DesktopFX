package com.ak.digitalfilter;

import com.ak.numbers.Coefficients;
import com.ak.numbers.Interpolators;
import com.ak.numbers.RangeUtils;
import com.ak.util.Builder;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnegative;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static com.ak.digitalfilter.IntsAcceptor.EMPTY_INTS;

public class FilterBuilder implements Builder<DigitalFilter> {
  private @Nullable DigitalFilter filter;

  private FilterBuilder() {
  }

  public static DigitalFilter parallel(List<int[]> selectedIndexes, DigitalFilter... filters) {
    if (selectedIndexes.isEmpty()) {
      throw new IllegalArgumentException(Arrays.deepToString(filters));
    }
    return of().fork(selectedIndexes, filters).buildNoDelay();
  }

  static DigitalFilter parallel(DigitalFilter... filters) {
    Objects.requireNonNull(filters);
    return parallel(Stream.generate(() -> EMPTY_INTS).limit(filters.length).toList(), filters);
  }

  public static FilterBuilder of() {
    return new FilterBuilder();
  }

  public FilterBuilder operator(Supplier<IntUnaryOperator> operatorSupplier) {
    return chain(new AbstractOperableFilter() {
      private final IntUnaryOperator operator = operatorSupplier.get();

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

  public FilterBuilder biOperator(Supplier<IntBinaryOperator> operatorSupplier) {
    return chain(new AbstractDigitalFilter() {
      private IntBinaryOperator operator = operatorSupplier.get();

      @Override
      public void reset() {
        operator = operatorSupplier.get();
      }

      @Override
      public int getOutputDataSize() {
        return 1;
      }

      @Override
      public void accept(int... values) {
        Objects.requireNonNull(values);
        publish(operator.applyAsInt(values[0], values[1]));
      }

      @Override
      public String toString() {
        return "BiOperator " + super.toString();
      }
    });
  }

  public FilterBuilder fir(Supplier<double[]> coefficients) {
    return fir(coefficients.get());
  }

  public FilterBuilder smoothingImpulsive(@Nonnegative int size) {
    var holdFilter = new HoldFilter.Builder(size).lostCount((size - Integer.highestOneBit(size)) / 2);
    return chain(holdFilter).chain(new DecimationFilter(size)).operator(() -> _ -> {
      int[] sorted = holdFilter.getSorted();
      double mean = Arrays.stream(sorted).average().orElse(0.0);

      var posCount = 0;
      var negCount = 0;
      var distances = 0.0;
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
    var holdFilter = new HoldFilter.Builder(size).lostCount(0);
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

  FilterBuilder expSum() {
    return chain(new ExpSumFilter());
  }

  FilterBuilder fir(double... coefficients) {
    return chain(new FIRFilter(coefficients));
  }

  public FilterBuilder average(@Nonnegative int count) {
    return chain(new FIRFilter(DoubleStream.generate(() -> 1.0 / count).limit(count).toArray()));
  }

  FilterBuilder iir(double... coefficients) {
    return chain(new IIRFilter(coefficients));
  }

  public FilterBuilder iirMATLAB(double[] num, double[] den) {
    return fir(RangeUtils.reverseOrder(num)).iir(Arrays.stream(den).skip(1).map(operand -> -operand).toArray());
  }

  private FilterBuilder comb(@Nonnegative int combFactor) {
    return chain(new CombFilter(combFactor));
  }

  FilterBuilder integrate() {
    return chain(new IntegrateFilter());
  }

  /**
   * Mean by Recursive Running Sum with <b>zero-delay</b>.
   *
   * @param averageFactor average factor.
   * @return FilterBuilder
   */
  FilterBuilder recursiveMean(@Nonnegative int averageFactor) {
    return chain(ExcessBufferFilter.mean(averageFactor));
  }

  public FilterBuilder rrs() {
    return chain(new RRSFilter());
  }

  FilterBuilder removeConstant(double alpha) {
    return chain(new RemoveConstantFilter(alpha));
  }

  /**
   * Standard Deviation by Recursive Running Sum with <b>zero-delay</b>.
   *
   * @param averageFactor average factor.
   * @return FilterBuilder
   */
  public FilterBuilder recursiveStd(@Nonnegative int averageFactor) {
    return wrap("recursiveStd%d".formatted(averageFactor),
        of().fork(new NoFilter(), ExcessBufferFilter.mean(averageFactor))
            .biOperator(() -> (x, mean) -> x - mean)
            .chain(ExcessBufferFilter.std2(averageFactor))
            .operator(() -> x -> (int) Math.sqrt(x))
    );
  }

  /**
   * Mean and Standard Deviation by Recursive Running Sum with <b>zero-delay</b>.
   *
   * @param averageFactor average factor.
   * @return FilterBuilder
   */
  FilterBuilder recursiveMeanAndStd(@Nonnegative int averageFactor) {
    return wrap("mean-n-std%d".formatted(averageFactor),
        of().fork(new NoFilter(), ExcessBufferFilter.mean(averageFactor))
            .fork(
                of().biOperator(() -> (_, mean) -> mean).build(),
                of().biOperator(() -> (x, mean) -> x - mean).chain(ExcessBufferFilter.std2(averageFactor))
                    .operator(() -> x -> (int) Math.sqrt(x)).build()
            )
    );
  }

  public FilterBuilder peakToPeak(@Nonnegative int size) {
    return chain(new PeakToPeakFilter(size));
  }

  public static <C extends Enum<C> & Coefficients> FilterBuilder asFilterBuilder(Class<C> coeffEnum) {
    return of().biOperator(Interpolators.interpolator(coeffEnum));
  }

  public static FilterBuilder asFilterBuilder(Coefficients coefficients) {
    return of().operator(Interpolators.interpolator(coefficients));
  }

  public FilterBuilder decimate(Supplier<double[]> coefficients, @Nonnegative int decimateFactor) {
    return chain(new FIRFilter(coefficients.get())).chain(new DecimationFilter(decimateFactor));
  }

  public FilterBuilder interpolate(@Nonnegative int interpolateFactor, Supplier<double[]> coefficients) {
    return chain(new InterpolationFilter(interpolateFactor)).chain(new FIRFilter(coefficients.get()));
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

  FilterBuilder fork(DigitalFilter... filters) {
    return fork(Collections.emptyList(), filters);
  }

  DigitalFilter buildNoDelay() {
    return new NoDelayFilter(build());
  }

  @Override
  public DigitalFilter build() {
    return filter == null ? new NoFilter() : filter;
  }

  public int[] filter(int[] ints) {
    DigitalFilter f = build();
    var result = new int[(int) Math.floor(ints.length * f.getFrequencyFactor())];
    var index = new AtomicInteger();
    f.forEach(values -> result[index.getAndIncrement()] = values[0]);
    for (int i : ints) {
      f.accept(i);
    }
    return result;
  }

  private FilterBuilder chain(DigitalFilter chain) {
    if (filter == null) {
      filter = Objects.requireNonNull(chain);
    }
    else {
      filter = new ChainFilter(filter, chain);
    }
    return this;
  }

  private FilterBuilder fork(List<int[]> selectedIndexes, DigitalFilter... filters) {
    Objects.requireNonNull(selectedIndexes);
    Objects.requireNonNull(filters);
    if (filters.length == 0) {
      throw new IllegalArgumentException();
    }
    DigitalFilter[] wrappedFilters;
    if (selectedIndexes.isEmpty()) {
      wrappedFilters = filters.clone();
    }
    else {
      if (selectedIndexes.size() != filters.length) {
        throw new IllegalArgumentException("selectedIndexes.length [%s] != filters.length [%s]"
            .formatted(selectedIndexes.stream().map(Arrays::toString).collect(Collectors.joining()), Arrays.toString(filters)));
      }
      wrappedFilters = new DigitalFilter[filters.length];
      for (var i = 0; i < wrappedFilters.length; i++) {
        int[] ints = selectedIndexes.get(i);
        if (ints.length == 0) {
          ints = new int[] {i};
        }
        wrappedFilters[i] = new SelectFilter(ints, filters[i]);
      }
    }
    return filters.length == 1 ? chain(wrappedFilters[0]) : chain(new ForkFilter(wrappedFilters));
  }

  private FilterBuilder wrap(String name, Builder<DigitalFilter> filterBuilder) {
    return chain(new FilterWrapper(name, filterBuilder.build()));
  }
}
