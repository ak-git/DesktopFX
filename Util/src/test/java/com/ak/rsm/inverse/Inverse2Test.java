package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
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
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0 - 0.5),
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0 - 0.5 * 2)
            ),
            Metrics.fromMilli(-1.0)
        },
        {
            List.of(
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0),
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0 + 0.1),
                TetrapolarDerivativeResistance.milli().dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0 + 0.1 * 2)
            ),
            Metrics.fromMilli(1.0)
        },
    };
  }


  @Test(dataProvider = "derivative-resistance-with-unknown-indent", enabled = false)
  public void test(@Nonnull Collection<Collection<? extends DerivativeResistivity>> ms, double maxIndent) {
    List<DynamicInverse> dynamicInverses = ms.stream().map(DynamicInverse::new).toList();

    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException(statisticsL.toString());
    }

    double L = statisticsL.getAverage();
    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> {
          Iterator<double[]> iterator = IntStream.range(0, dynamicInverses.size()).mapToObj(i -> {
            double[] kwIndent = kw.clone();
            kwIndent[1] += kwIndent[2] * i / L;
            return kwIndent;
          }).iterator();

          return dynamicInverses.stream().mapToDouble(value -> value.applyAsDouble(iterator.next()))
              .reduce(StrictMath::hypot).orElseThrow();
        },
        new SimpleBounds(new double[] {-1.0, 0.0, Math.min(maxIndent, 0.0)}, new double[] {1.0, 1.0, Math.max(maxIndent, 0.0)}),
        new double[] {0.01, 0.01, Math.abs(maxIndent) / 100.0}
    );
    Logger.getAnonymousLogger().info(() -> "%.6f; h = %.1f mm; indent = %.1f mm"
        .formatted(kwOptimal.getValue(), Metrics.toMilli(kwOptimal.getPoint()[1] * L), Metrics.toMilli(kwOptimal.getPoint()[2]))
    );
  }
}
