package com.ak.rsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.ToDoubleBiFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;

import static java.lang.StrictMath.exp;
import static java.lang.StrictMath.log;

enum Measurements {
  ;

  @Nonnegative
  static double getBaseL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.parallelStream().mapToDouble(m -> m.getSystem().getL()).max().orElseThrow();
  }

  @Nonnegative
  static double getMaxHToL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.parallelStream()
        .mapToDouble(measurement -> measurement.getSystem().getHMax(1.0)).min().orElseThrow() / getBaseL(measurements);
  }

  @Nonnull
  static ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted(@Nonnull Collection<? extends Measurement> measurements) {
    double baseL = getBaseL(measurements);
    return (s, kw) -> new Log1pApparent2Rho(s.toRelative()).value(kw[0], kw[1] * baseL / s.getL());
  }

  @Nonnull
  static ToDoubleBiFunction<TetrapolarSystem, double[]> logDiffApparentPredicted(@Nonnull Collection<? extends Measurement> measurements) {
    double baseL = getBaseL(measurements);
    return (s, kw) -> log(Math.abs(new DerivativeApparentByPhi2Rho(s.toRelative()).value(kw[0], kw[1] * baseL / s.getL())));
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static ValuePair getRho1(Collection<? extends Measurement> measurements, RelativeMediumLayers<Double> kw) {
    if (RelativeMediumLayers.SINGLE_LAYER.equals(kw)) {
      Measurement average = measurements.stream().map(Measurement.class::cast).reduce(Measurement::merge).orElseThrow();
      double rho = average.getResistivity();
      return new ValuePair(rho, rho * average.getSystem().getApparentRelativeError());
    }
    else {
      double sumLogApparent = measurements.stream().mapToDouble(x -> log(x.getResistivity())).sum();
      var logApparentPredicted = logApparentPredicted(measurements);
      double sumLogApparentPredicted = measurements.stream()
          .map(Measurement::getSystem)
          .mapToDouble(s -> logApparentPredicted.applyAsDouble(s, new double[] {kw.k12(), kw.hToL()})).sum();
      return new ValuePair(exp((sumLogApparent - sumLogApparentPredicted) / measurements.size()));
    }
  }

  @Nonnull
  static Collection<Collection<Measurement>> errors(@Nonnull Collection<? extends Measurement> measurements) {
    return TetrapolarSystem.getMeasurementsCombination(measurements.stream().map(Measurement::getSystem).toList())
        .stream().map(systems -> {
          Collection<Measurement> ms = new ArrayList<>(systems.size());
          Iterator<TetrapolarSystem> tsIterator = systems.iterator();
          Iterator<? extends Measurement> mIterator = measurements.iterator();
          while (tsIterator.hasNext() && mIterator.hasNext()) {
            ms.add(mIterator.next().newInstance(tsIterator.next()));
          }
          return ms;
        })
        .toList();
  }
}
