package com.ak.rsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

interface Measurement {
  @Nonnull
  InexactTetrapolarSystem getSystem();

  @Nonnegative
  double getResistivity();

  default double getLogResistivity() {
    return StrictMath.log(getResistivity());
  }

  @Nonnull
  default Measurement merge(@Nonnull Measurement that) {
    throw new UnsupportedOperationException(that.toString());
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static Measurement newInstance(Measurement m, InexactTetrapolarSystem s) {
    return new Measurement() {
      @Nonnull
      @Override
      public InexactTetrapolarSystem getSystem() {
        return s;
      }

      @Override
      public double getResistivity() {
        return m.getResistivity();
      }
    };
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static <T extends Measurement> Collection<Collection<T>> getMeasurementsCombination(
      Collection<T> systems, BiFunction<T, InexactTetrapolarSystem, T> newInstance) {
    ToLongFunction<Collection<InexactTetrapolarSystem>> distinctSizes =
        ts -> ts.stream().flatMap(s -> DoubleStream.of(s.toExact().getS(), s.toExact().getL()).boxed()).distinct().count();
    var initialSizes = distinctSizes.applyAsLong(systems.stream().map(Measurement::getSystem).collect(Collectors.toUnmodifiableList()));
    return IntStream.range(0, 2 << (2 * (systems.size() - 1) + 1))
        .mapToObj(n -> {
          var signIndex = new AtomicInteger();
          IntUnaryOperator sign = index -> (n & (1 << index)) == 0 ? 1 : -1;
          return systems.stream()
              .map(s -> s.getSystem().shift(
                  sign.applyAsInt(signIndex.getAndIncrement()),
                  sign.applyAsInt(signIndex.getAndIncrement())))
              .collect(Collectors.toUnmodifiableList());
        })
        .filter(s -> initialSizes == distinctSizes.applyAsLong(s))
        .map(systemsWithShifts -> {
          Iterator<InexactTetrapolarSystem> iteratorS = systemsWithShifts.iterator();
          Iterator<T> iteratorM = systems.iterator();
          List<T> result = new ArrayList<>();
          while (iteratorS.hasNext() || iteratorM.hasNext()) {
            result.add(newInstance.apply(iteratorM.next(), iteratorS.next()));
          }
          return result;
        })
        .collect(Collectors.toUnmodifiableList());
  }
}
