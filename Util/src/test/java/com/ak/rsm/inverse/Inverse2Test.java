package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.medium.MediumLayers;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.util.Metrics;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Inverse2Test {
  @DataProvider(name = "noChanged")
  public static Object[][] noChanged() {
    double[] indentationsMilli = {0, -0.5, -1.0};
    return new Object[][] {
        {
            Arrays.stream(indentationsMilli).mapToObj(mm ->
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system2(6.0)
                    .rho1(9.0).rho2(1.0).h(2.0 - mm)
            ).toList(),
            Arrays.stream(indentationsMilli).map(Metrics::fromMilli).toArray()
        },
    };
  }

  @Test(dataProvider = "noChanged", enabled = false)
  @ParametersAreNonnullByDefault
  public void testNoChanged(Collection<Collection<? extends DerivativeMeasurement>> ms, double[] indentations) {
    List<ToDoubleFunction<double[]>> dynamicInverses = ms.stream().map(DynamicInverse::of).toList();

    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException("L is not equal for all electrode systems %s".formatted(statisticsL));
    }

    double L = statisticsL.getAverage();
    PointValuePair kwOptimal = Simplex.optimize(
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
    List<Layer2Medium> mediumList = ms.stream().map(dm -> new Layer2Medium(dm, new Layer2RelativeMedium(kwOptimal.getPoint()))).toList();
    var rho1 = mediumList.stream().map(MediumLayers::rho1).reduce(ValuePair::mergeWith).orElseThrow();
    var rho2 = mediumList.stream().map(MediumLayers::rho2).reduce(ValuePair::mergeWith).orElseThrow();
    var h = mediumList.stream().map(MediumLayers::h1).reduce(ValuePair::mergeWith).orElseThrow();
    Logger.getAnonymousLogger().info(() -> "%.6f; %s; %s; %s".formatted(kwOptimal.getValue(), rho1, rho2, h));
  }

  @DataProvider(name = "kChanged")
  public static Object[][] kChanged() {
    return new Object[][] {
        {
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system2(6.0).rho1(4.0).rho2(1.0).h(2.0 - 0.5),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system2(6.0).rho1(4.0).rho2(1.0).h(2.0 - 1.0)
            ),
            0.2,
            new double[] {0.0, Metrics.fromMilli(-0.5), Metrics.fromMilli(-1.0)}
        },
    };
  }


  @Test(dataProvider = "kChanged", enabled = false)
  @ParametersAreNonnullByDefault
  public void testKChanged(Collection<Collection<? extends DerivativeMeasurement>> ms, double maxKChanges, double[] indentations) {
    List<ToDoubleFunction<double[]>> dynamicInverses = ms.stream().map(DynamicInverse::of).toList();

    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException(statisticsL.toString());
    }
    double L = statisticsL.getAverage();

    double[] lB = DoubleStream
        .concat(
            DoubleStream.of(-1.0, 0.0),
            DoubleStream.generate(() -> Math.min(maxKChanges, 0.0)).limit(dynamicInverses.size() - 1)
        )
        .toArray();

    double[] uB = DoubleStream
        .concat(
            DoubleStream.of(1.0, 1.0),
            DoubleStream.generate(() -> Math.max(maxKChanges, 0.0)).limit(dynamicInverses.size() - 1)
        )
        .toArray();

    double[] initialSteps = IntStream.range(0, Math.max(lB.length, uB.length))
        .mapToDouble(i ->
            Math.abs(uB[i] - lB[i]) / 100.0
        )
        .toArray();

    PointValuePair kwOptimal = Simplex.optimize(
        kw -> {
          Iterator<double[]> iterator = IntStream.range(0, dynamicInverses.size()).mapToObj(i -> {
            double[] kwIndent = Arrays.copyOf(kw, 2);
            double[] changes = Arrays.copyOfRange(kw, kwIndent.length, kw.length);
            for (int j = 0; j <= i - 1; j++) {
              kwIndent[0] += changes[j];
            }
            kwIndent[1] += indentations[i] / L;
            return kwIndent;
          }).iterator();

          return dynamicInverses.stream().mapToDouble(value -> value.applyAsDouble(iterator.next()))
              .reduce(StrictMath::hypot).orElseThrow();
        },
        new SimpleBounds(lB, uB), initialSteps
    );

    List<Layer2Medium> mediumList = ms.stream().map(dm -> new Layer2Medium(dm, new Layer2RelativeMedium(kwOptimal.getPoint()))).toList();
    var rho1 = mediumList.stream().map(MediumLayers::rho1).reduce(ValuePair::mergeWith).orElseThrow();
    var rho2 = mediumList.stream().map(MediumLayers::rho2).reduce(ValuePair::mergeWith).orElseThrow();
    var h = mediumList.stream().map(MediumLayers::h1).reduce(ValuePair::mergeWith).orElseThrow();
    Logger.getAnonymousLogger().info(() -> "%.6f; %s; %s; %s".formatted(kwOptimal.getValue(), rho1, rho2, h));
    Logger.getAnonymousLogger().info(() -> {
          double[] changes = Arrays.copyOfRange(kwOptimal.getPoint(), 2, kwOptimal.getPoint().length);
          String kChanges = Arrays.stream(changes)
              .mapToObj("%.2f"::formatted).collect(Collectors.joining("; ", "[", "]"));
          String hIndent = Arrays.stream(indentations).map(Metrics::toMilli)
              .mapToObj("%.2f"::formatted).collect(Collectors.joining("; ", "[", "]"));
          return "%.6f; k = %.2f; h = %.1f mm; kChanges = %s; indent = %s mm%n"
              .formatted(kwOptimal.getValue(), kwOptimal.getPoint()[0], Metrics.toMilli(kwOptimal.getPoint()[1] * L),
                  kChanges, hIndent
              );
        }
    );
  }

  @DataProvider(name = "kAndPhiChanged")
  public static Object[][] kAndPhiChanged() {
    return new Object[][] {
        {
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system2(6.0).rho1(5.0).rho2(1.0).h(2.0),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system2(6.0).rho1(4.0).rho2(1.0).h(2.0 - 0.4),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system2(6.0).rho1(4.0).rho2(1.0).h(2.0 - 1.0)
            ),
            0.1,
            Metrics.fromMilli(-1.0)
        },
    };
  }

  @Test(dataProvider = "kAndPhiChanged", enabled = false)
  public void testKAndPhiChanged(@Nonnull Collection<Collection<? extends DerivativeMeasurement>> ms, double maxKChanges, double maxIndent) {
    List<ToDoubleFunction<double[]>> dynamicInverses = ms.stream().map(DynamicInverse::of).toList();

    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException(statisticsL.toString());
    }
    double L = statisticsL.getAverage();

    double[] lB = DoubleStream
        .concat(
            DoubleStream.of(-1.0, 0.0),
            DoubleStream.concat(
                DoubleStream.generate(() -> Math.min(maxKChanges, 0.0)).limit(dynamicInverses.size() - 1),
                DoubleStream.generate(() -> Math.min(maxIndent, 0.0)).limit(dynamicInverses.size() - 1)
            )
        )
        .toArray();

    double[] uB = DoubleStream
        .concat(
            DoubleStream.of(1.0, 1.0),
            DoubleStream.concat(
                DoubleStream.generate(() -> Math.max(maxKChanges, 0.0)).limit(dynamicInverses.size() - 1),
                DoubleStream.generate(() -> Math.max(maxIndent, 0.0)).limit(dynamicInverses.size() - 1)
            )
        )
        .toArray();

    double[] initialSteps = IntStream.range(0, Math.max(lB.length, uB.length))
        .mapToDouble(i ->
            Math.abs(uB[i] - lB[i]) / 100.0
        )
        .toArray();

    PointValuePair kwOptimal = Simplex.optimize(
        kw -> {
          Iterator<double[]> iterator = IntStream.range(0, dynamicInverses.size()).mapToObj(i -> {
            double[] kwIndent = Arrays.copyOf(kw, 2);
            double[] changes = Arrays.copyOfRange(kw, kwIndent.length, kw.length);
            for (int j = 0; j <= i - 1; j++) {
              kwIndent[0] += changes[j];
            }
            for (int j = 0; j <= i - 1; j++) {
              kwIndent[1] += changes[changes.length / 2 + j] / L;
            }
            return kwIndent;
          }).iterator();

          return dynamicInverses.stream().mapToDouble(value -> value.applyAsDouble(iterator.next()))
              .reduce(StrictMath::hypot).orElseThrow();
        },
        new SimpleBounds(lB, uB), initialSteps
    );

    List<Layer2Medium> mediumList = ms.stream().map(dm -> new Layer2Medium(dm, new Layer2RelativeMedium(kwOptimal.getPoint()))).toList();
    var rho1 = mediumList.stream().map(MediumLayers::rho1).reduce(ValuePair::mergeWith).orElseThrow();
    var rho2 = mediumList.stream().map(MediumLayers::rho2).reduce(ValuePair::mergeWith).orElseThrow();
    var h = mediumList.stream().map(MediumLayers::h1).reduce(ValuePair::mergeWith).orElseThrow();
    Logger.getAnonymousLogger().info(() -> "%.6f; %s; %s; %s".formatted(kwOptimal.getValue(), rho1, rho2, h));

    Logger.getAnonymousLogger().info(() -> {
          double[] changes = Arrays.copyOfRange(kwOptimal.getPoint(), 2, kwOptimal.getPoint().length);
          String kChanges = Arrays.stream(changes).limit(changes.length / 2)
              .mapToObj("%.2f"::formatted).collect(Collectors.joining("; ", "[", "]"));
          String hIndent = Arrays.stream(changes).skip(changes.length / 2).map(Metrics::toMilli)
              .mapToObj("%.2f"::formatted).collect(Collectors.joining("; ", "[", "]"));

          return "%.6f; k = %.2f; h = %.1f mm; kChanges = %s; indent = %s mm%n"
              .formatted(kwOptimal.getValue(), kwOptimal.getPoint()[0], Metrics.toMilli(kwOptimal.getPoint()[1] * L),
                  kChanges, hIndent
              );
        }
    );
  }
}
