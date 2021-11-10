package com.ak.rsm;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;

record TetrapolarMeasurement(@Nonnull TetrapolarSystem system, @Nonnegative double resistivity) implements Measurement {
  private static final Function<Measurement, ValuePair> TO_VALUE =
      m -> ValuePair.Name.RHO_1.of(m.resistivity(), m.resistivity() * m.system().getApparentRelativeError());

  @Override
  @Nonnull
  public Prediction toPrediction(@Nonnull RelativeMediumLayers kw, @Nonnegative double rho1) {
    return new TetrapolarPrediction(system, kw, rho1, resistivity);
  }

  @Nonnull
  @Override
  public Measurement merge(@Nonnull Measurement that) {
    var avg = TO_VALUE.apply(this).mergeWith(TO_VALUE.apply(that));
    double relErrorRho = avg.getAbsError() / avg.getValue();
    double dL = Math.min(system.getAbsError(), that.system().getAbsError());
    double lCC = RelativeTetrapolarSystem.MIN_ERROR_FACTOR * dL / relErrorRho;
    double sPU = RelativeTetrapolarSystem.OPTIMAL_SL * lCC;
    TetrapolarSystem merged = TetrapolarSystem.si(dL).s(sPU).l(lCC);
    return new TetrapolarMeasurement(merged, avg.getValue());
  }

  @Override
  public String toString() {
    return "%s; %s".formatted(system,
        ValuePair.Name.RHO_1.of(resistivity, resistivity * system.getApparentRelativeError())
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !getClass().equals(o.getClass())) {
      return false;
    }
    TetrapolarMeasurement that = (TetrapolarMeasurement) o;
    return Double.compare(that.resistivity, resistivity) == 0 && system.equals(that.system);
  }

  @Override
  public int hashCode() {
    return Objects.hash(system, resistivity);
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static List<Measurement> of(TetrapolarSystem[] systems, double[] ohms) {
    var ohmsIt = DoubleStream.of(ohms).iterator();
    return Arrays.stream(systems)
        .map(s -> new TetrapolarMeasurement(s, s.getApparent(ohmsIt.nextDouble())))
        .map(Measurement.class::cast).toList();
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static List<Measurement> of(TetrapolarSystem[] systems, ToDoubleFunction<TetrapolarSystem> toOhms) {
    return Arrays.stream(systems).map(s -> new TetrapolarMeasurement(s, s.getApparent(toOhms.applyAsDouble(s))))
        .map(Measurement.class::cast).toList();
  }
}
