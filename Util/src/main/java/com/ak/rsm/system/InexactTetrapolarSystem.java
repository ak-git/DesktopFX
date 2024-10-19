package com.ak.rsm.system;

import com.ak.util.Metrics;

import javax.annotation.Nonnegative;
import javax.measure.MetricPrefix;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import java.util.function.ToLongFunction;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static tech.units.indriya.unit.Units.METRE;

public record InexactTetrapolarSystem(@Nonnegative double absError, TetrapolarSystem system) {
  public InexactTetrapolarSystem(@Nonnegative double absError, TetrapolarSystem system) {
    this.absError = Math.abs(absError);
    this.system = system;
  }

  @Nonnegative
  public double getHMax(double k) {
    return system.relativeSystem().hMaxFactor(k) * system.getDim() / StrictMath.pow(getLRelativeError(), 1.0 / 3.0);
  }

  @Nonnegative
  public double getHMin(double k) {
    return system.getDim() * Math.sqrt(getLRelativeError()) * system.relativeSystem().hMinFactor(k);
  }

  /**
   * dRho / Rho = E * dL / L
   *
   * @return relative apparent error
   */
  @Nonnegative
  public double getApparentRelativeError() {
    return Math.abs(system.relativeSystem().errorFactor() * getLRelativeError());
  }

  @Nonnegative
  double getLRelativeError() {
    return absError / system.getDim();
  }

  @Override
  public String toString() {
    String s = system.toString();
    if (absError > 0) {
      double metre = getHMax(1.0);
      return "%s / %.1f %s; ↕ %.0f %s".formatted(
          s, Metrics.Length.METRE.to(absError, MetricPrefix.MILLI(METRE)), MetricPrefix.MILLI(METRE),
          Metrics.Length.METRE.to(metre, MetricPrefix.MILLI(METRE)), MetricPrefix.MILLI(METRE));
    }
    else {
      return s;
    }
  }

  @Nonnegative
  public static double getBaseL(Collection<InexactTetrapolarSystem> inexactSystems) {
    return TetrapolarSystem.getBaseL(inexactSystems.stream().map(InexactTetrapolarSystem::system).toList());
  }

  public static Collection<List<TetrapolarSystem>> getMeasurementsCombination(Collection<InexactTetrapolarSystem> systems) {
    ToLongFunction<Collection<TetrapolarSystem>> distinctSizes =
        ts -> ts.stream().flatMap(s -> DoubleStream.of(s.sPU(), s.lCC()).boxed()).distinct().count();
    var initialSizes = distinctSizes.applyAsLong(systems.stream().map(InexactTetrapolarSystem::system).toList());
    return IntStream.range(0, 1 << systems.size())
        .mapToObj(n -> {
          var signIndex = new AtomicInteger();
          IntUnaryOperator sign = index -> (n & (1 << index)) == 0 ? 1 : -1;
          return systems.stream().map(s -> s.shift(sign.applyAsInt(signIndex.getAndIncrement())).system).toList();
        })
        .filter(s -> initialSizes == distinctSizes.applyAsLong(s)).toList();
  }

  private InexactTetrapolarSystem shift(int sign) {
    double err = Math.signum(sign) * absError;
    double sPU = system.sPU();
    double lCC = system.lCC();
    if (sPU < lCC) {
      err *= -1.0;
    }
    return new InexactTetrapolarSystem(absError, new TetrapolarSystem(sPU + err, lCC - err));
  }
}
