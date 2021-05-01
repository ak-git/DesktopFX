package com.ak.rsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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
    return InexactTetrapolarSystem.getMeasurementsCombination(systems.stream().map(Measurement::getSystem).collect(Collectors.toUnmodifiableList()))
        .stream()
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
