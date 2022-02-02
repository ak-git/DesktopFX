package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.util.Metrics;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Inverse2Test {
  @DataProvider(name = "derivative-resistance-with-fixed-indent")
  public static Object[][] derivativeResistanceWithFixedIndent() {
    return new Object[][] {
        {
            List.of(
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0),
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0 - 0.5),
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0 - 1.0)
            ),
            new double[] {0.0, Metrics.fromMilli(-0.5), Metrics.fromMilli(-1.0)}
        },
    };
  }


  @Test(dataProvider = "derivative-resistance-with-fixed-indent", enabled = false)
  @ParametersAreNonnullByDefault
  public void test(Collection<Collection<? extends DerivativeResistivity>> ms, double[] indentations) {
    List<DynamicInverse> dynamicInverses = ms.stream().map(DynamicInverse::new).toList();

    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException(statisticsL.toString());
    }

    double L = statisticsL.getAverage();
    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> {
          Iterator<double[]> iterator = Arrays.stream(indentations).mapToObj(x -> {
            double[] kwIndent = kw.clone();
            kwIndent[1] += x / L;
            return kwIndent;
          }).iterator();

          return dynamicInverses.stream().mapToDouble(value -> value.applyAsDouble(iterator.next()))
              .reduce(StrictMath::hypot).orElseThrow();
        },
        new SimpleBounds(new double[] {-1.0, 0}, new double[] {1.0, 1.0}),
        new double[] {0.01, 0.01}
    );
    Logger.getAnonymousLogger().info(() -> "%.6f; h = %.1f mm"
        .formatted(kwOptimal.getValue(), Metrics.toMilli(kwOptimal.getPoint()[1] * L))
    );
  }

  @DataProvider(name = "derivative-resistance-with-unknown-indent")
  public static Object[][] derivativeResistanceWithUnknownIndent() {
    return new Object[][] {
        {
            List.of(
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0),
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0 - 0.4),
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0 - 1.0)
            ),
            Metrics.fromMilli(-1.0)
        },
        {
            List.of(
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0),
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0 + 0.4),
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0 + 1.0)
            ),
            Metrics.fromMilli(1.0)
        },
        {
            List.of(
                TetrapolarDerivativeResistance.milli().dh(Double.NaN).system2(6.0)
                    .rho(
                        4.36484833090749, 4.49332876699692,
                        -0.168318891626108, -0.182683171577791
                    ),
                TetrapolarDerivativeResistance.milli().dh(Double.NaN).system2(6.0)
                    .rho(
                        4.37027448440132, 4.4985260054503,
                        -0.196894129863152, -0.243242343614173
                    ),
                TetrapolarDerivativeResistance.milli().dh(Double.NaN).system2(6.0)
                    .rho(
                        4.37794989890251, 4.50886362514046,
                        -0.224049353755586, -0.310504288925638
                    )
            ),
            Metrics.fromMilli(-10.0)
        },
    };
  }

  @Test(dataProvider = "derivative-resistance-with-unknown-indent", invocationCount = 3, enabled = false)
  public void test(@Nonnull Collection<Collection<? extends DerivativeResistivity>> ms, double maxIndent) {
    List<DynamicInverse> dynamicInverses = ms.stream().map(DynamicInverse::new).toList();

    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException(statisticsL.toString());
    }

    double L = statisticsL.getAverage();
    double[] lB = DoubleStream
        .concat(
            DoubleStream.of(-1.0, 0.0), DoubleStream.generate(() -> Math.min(maxIndent, 0.0)).limit(dynamicInverses.size() - 1)
        )
        .toArray();

    double[] uB = DoubleStream
        .concat(
            DoubleStream.of(1.0, 1.0), DoubleStream.generate(() -> Math.max(maxIndent, 0.0)).limit(dynamicInverses.size() - 1)
        )
        .toArray();

    double[] initialSteps = IntStream.range(0, Math.max(lB.length, uB.length))
        .mapToDouble(i ->
            Math.abs(uB[i] - lB[i]) / 100.0
        ).toArray();

    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> {
          Iterator<double[]> iterator = IntStream.range(0, dynamicInverses.size()).mapToObj(i -> {
            double[] kwIndent = Arrays.copyOf(kw, 2);
            if (i > 0) {
              kwIndent[1] += kw[kwIndent.length + (i - 1)] / L;
            }
            return kwIndent;
          }).iterator();

          return dynamicInverses.stream().mapToDouble(value -> value.applyAsDouble(iterator.next()))
              .reduce(StrictMath::hypot).orElseThrow();
        },
        new SimpleBounds(lB, uB), initialSteps
    );
    Logger.getAnonymousLogger().info(() -> "%.6f; k = %.2f; h = %.1f mm; indent = %s mm"
        .formatted(kwOptimal.getValue(), kwOptimal.getPoint()[0], Metrics.toMilli(kwOptimal.getPoint()[1] * L),
            Arrays.stream(Arrays.copyOfRange(kwOptimal.getPoint(), 2, kwOptimal.getPoint().length)).map(Metrics::toMilli)
                .mapToObj("%.2f"::formatted).collect(Collectors.joining("; ", "[", "]"))
        )
    );
  }
}
