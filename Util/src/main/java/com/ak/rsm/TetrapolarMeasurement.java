package com.ak.rsm;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;

import com.ak.math.ValuePair;

@ThreadSafe
final class TetrapolarMeasurement implements Measurement {
  private static final Function<Measurement, ValuePair> TO_VALUE =
      m -> ValuePair.Name.RHO_1.of(m.getResistivity(), m.getResistivity() * m.getSystem().getApparentRelativeError());
  @Nonnull
  private final TetrapolarSystem system;
  @Nonnegative
  private final double resistivity;

  TetrapolarMeasurement(@Nonnull TetrapolarSystem system, @Nonnegative double rOhms) {
    this.system = system;
    resistivity = system.getApparent(rOhms);
  }

  @Override
  @Nonnull
  public TetrapolarSystem getSystem() {
    return system;
  }

  @Override
  @Nonnegative
  public double getResistivity() {
    return resistivity;
  }

  @Override
  @Nonnull
  public Prediction toPrediction(@Nonnull RelativeMediumLayers kw, @Nonnegative double rho1) {
    return new TetrapolarPrediction(getSystem(), kw, rho1, resistivity);
  }

  @Nonnull
  @Override
  public Measurement merge(@Nonnull Measurement that) {
    var avg = TO_VALUE.apply(this).mergeWith(TO_VALUE.apply(that));
    double relErrorRho = avg.getAbsError() / avg.getValue();
    double dL = Math.min(system.getAbsError(), that.getSystem().getAbsError());
    double lCC = RelativeTetrapolarSystem.MIN_ERROR_FACTOR * dL / relErrorRho;
    double sPU = RelativeTetrapolarSystem.OPTIMAL_SL * lCC;
    TetrapolarSystem merged = TetrapolarSystem.si(dL).s(sPU).l(lCC);
    return new TetrapolarMeasurement(merged, new Resistance1Layer(merged).value(avg.getValue()));
  }

  @Override
  public String toString() {
    return "%s; %s".formatted(String.valueOf(system),
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
    return IntStream.range(0, systems.length)
        .mapToObj(i -> new TetrapolarMeasurement(systems[i], ohms[i])).map(Measurement.class::cast).toList();
  }
}
