package com.ak.rsm;

import java.util.Collection;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;

import com.ak.util.Metrics;
import com.ak.util.Strings;

@ThreadSafe
final class TetrapolarMeasurement implements Measurement {
  private static final ToDoubleFunction<Measurement> POW2 =
      m -> StrictMath.pow(m.getResistivity() * m.getSystem().getApparentRelativeError(), 2.0);
  @Nonnull
  private final InexactTetrapolarSystem system;
  @Nonnegative
  private final double resistivity;

  TetrapolarMeasurement(@Nonnull InexactTetrapolarSystem system, @Nonnegative double rOhms) {
    this.system = system;
    resistivity = system.toExact().getApparent(rOhms);
  }

  @Override
  @Nonnull
  public InexactTetrapolarSystem getSystem() {
    return system;
  }

  @Override
  @Nonnegative
  public double getResistivity() {
    return resistivity;
  }

  @Override
  @Nonnull
  public Prediction toPrediction(@Nonnull RelativeMediumLayers<Double> kw, @Nonnegative double rho1) {
    return new TetrapolarPrediction(getSystem(), kw, rho1, resistivity);
  }

  @Nonnull
  @Override
  public Measurement merge(@Nonnull Measurement that) {
    var sigma1Q = POW2.applyAsDouble(this);
    var sigma2Q = POW2.applyAsDouble(that);
    double k = sigma2Q / (sigma1Q + sigma2Q);
    double avg = k * resistivity + (1.0 - k) * that.getResistivity();
    double sigmaAvg = 1.0 / Math.sqrt((1.0 / sigma1Q + 1.0 / sigma2Q));

    double relErrorRho = sigmaAvg / avg;
    double dL = Math.min(system.getAbsError(), that.getSystem().getAbsError());
    double lCC = RelativeTetrapolarSystem.MIN_ERROR_FACTOR * dL / relErrorRho;
    double sPU = RelativeTetrapolarSystem.OPTIMAL_SL * lCC;
    InexactTetrapolarSystem merged = InexactTetrapolarSystem.si(dL).s(sPU).l(lCC);
    return new TetrapolarMeasurement(merged, new Resistance1Layer(merged.toExact()).value(avg));
  }

  @Override
  public String toString() {
    return "%s; %s (%.0f %%)".formatted(String.valueOf(system),
        Strings.rho(resistivity),
        Metrics.toPercents(system.getApparentRelativeError())
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
  static Collection<Measurement> of(InexactTetrapolarSystem[] systems, double[] ohms) {
    return IntStream.range(0, systems.length)
        .mapToObj(i -> new TetrapolarMeasurement(systems[i], ohms[i]))
        .collect(Collectors.toUnmodifiableList());
  }
}
