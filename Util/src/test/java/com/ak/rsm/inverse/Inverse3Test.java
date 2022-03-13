package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent3Rho;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.ConcurrentCache;
import com.ak.util.Metrics;
import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.MeanAlterer;
import io.jenetics.Mutator;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.Limits;
import io.jenetics.util.IntRange;
import org.apache.commons.math3.optim.PointValuePair;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;

public class Inverse3Test {
  @DataProvider(name = "single")
  public static Object[][] single() {
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.001).system4(10.0)
                .rho1(9.0).rho2(1.0).rho3(9.0).hStep(1.0).p(5, 5),
        },
    };
  }

  @Test(dataProvider = "single", enabled = false)
  public void testSingle(@Nonnull Collection<? extends DerivativeMeasurement> ms) {
    double hStep = Metrics.fromMilli(1.0);
    ToDoubleFunction<double[]> dynamicInverse = DynamicInverse.of(ms, hStep);

    int[] lB = {1, 1};
    int[] uB = {100, 100};

    record P(@Nonnegative int p1, @Nonnegative int p2mp1) {
      P(@Nonnull int[] p) {
        this(p[0], p[1]);
      }

      P(@Nonnull double[] p) {
        this((int) Math.round(p[0]), (int) Math.round(p[1]));
      }
    }

    Function<P, PointValuePair> cache = new ConcurrentCache<>(p -> {
      Logger.getAnonymousLogger().info(p::toString);
      return Simplex.optimizeAll(
          kw -> dynamicInverse.applyAsDouble(new double[] {kw[0], kw[1], p.p1, p.p2mp1}),
          new double[] {-1.0, 1.0}, new double[] {-1.0, 1.0}
      );
    });

    Phenotype<IntegerGene, Double> phenotype = Engine
        .builder(
            p -> cache.apply(new P(p)).getValue(),
            Codecs.ofVector(
                IntStream.range(0, lB.length)
                    .mapToObj(i -> IntRange.of(lB[i], uB[i]))
                    .toArray(IntRange[]::new)
            )
        )
        .populationSize(1 << 4)
        .optimize(Optimize.MINIMUM)
        .alterers(new Mutator<>(0.03), new MeanAlterer<>(0.6))
        .build().stream()
        .limit(Limits.bySteadyFitness(7))
        .limit(100)
        .peek(r -> Logger.getAnonymousLogger().info(() -> r.bestPhenotype().toString()))
        .collect(toBestPhenotype());

    Genotype<IntegerGene> best = phenotype.genotype();
    PointValuePair pOptimal = new PointValuePair(
        IntStream.range(0, best.length()).mapToDouble(i -> best.get(i).get(0).doubleValue()).toArray(),
        phenotype.fitness()
    );

    double[] point = pOptimal.getPoint();
    PointValuePair kwOptimal = cache.apply(new P(point));

    double[] kwpp = {kwOptimal.getPoint()[0], kwOptimal.getPoint()[1], point[0], point[1]};
    var rho1 = getRho1(ms, kwpp, hStep);
    var rho2 = ValuePair.Name.RHO_2.of(rho1.value() / Layers.getRho1ToRho2(kwpp[0]), 0.0);
    var rho3 = ValuePair.Name.RHO_3.of(rho2.value() / Layers.getRho1ToRho2(kwpp[1]), 0.0);
    Logger.getAnonymousLogger().info(() -> "%.6f %s; %s; %s; %s"
        .formatted(kwOptimal.getValue(), Arrays.toString(kwpp), rho1, rho2, rho3));
  }

  @DataProvider(name = "noChanged")
  public static Object[][] noChanged() {
    int[] indentations = {0, -5, -10};
    return new Object[][] {
        {
            Arrays.stream(indentations).mapToObj(i ->
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.0001).system2(6.0)
                    .rho1(9.0).rho2(1.0).rho3(9.0).hStep(0.1).p(10, 50 + i)
            ).toList(),
            indentations
        },
    };
  }

  @Test(dataProvider = "noChanged")
  @ParametersAreNonnullByDefault
  public void testNoChanged(Collection<Collection<? extends DerivativeMeasurement>> ms, int[] indentations) {
    double hStep = Metrics.fromMilli(0.1);
    List<ToDoubleFunction<double[]>> dynamicInverses = ms.stream().map(dm -> DynamicInverse.of(dm, hStep)).toList();

    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException("L is not equal for all electrode systems %s".formatted(statisticsL));
    }

    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> {
          Iterator<double[]> iterator = Arrays.stream(indentations).mapToObj(x -> {
            double[] kwIndent = kw.clone();
            kwIndent[3] += x;
            return kwIndent;
          }).iterator();

          return dynamicInverses.stream().mapToDouble(value -> value.applyAsDouble(iterator.next()))
              .reduce(StrictMath::hypot).orElseThrow();
        },
        new double[] {-1.0, 1.0}, new double[] {-1.0, 1.0},
        new double[] {1, 100}, new double[] {1 + Arrays.stream(indentations).map(Math::abs).max().orElse(0), 100}
    );

    var rho1 = ms.stream().map(dm -> getRho1(dm, kwOptimal.getPoint(), hStep)).reduce(ValuePair::mergeWith).orElseThrow();
    var rho2 = ValuePair.Name.RHO_2.of(rho1.value() / Layers.getRho1ToRho2(kwOptimal.getPoint()[0]), 0.0);
    var rho3 = ValuePair.Name.RHO_3.of(rho2.value() / Layers.getRho1ToRho2(kwOptimal.getPoint()[1]), 0.0);
    Logger.getAnonymousLogger().info(() -> "%.6f %s; %s; %s; %s"
        .formatted(kwOptimal.getValue(), Arrays.toString(kwOptimal.getPoint()), rho1, rho2, rho3));
  }

  @Nonnegative
  @ParametersAreNonnullByDefault
  private static ValuePair getRho1(Collection<? extends DerivativeMeasurement> measurements, double[] kw, @Nonnegative double hStep) {
    return measurements.stream().parallel()
        .map(measurement -> {
          TetrapolarSystem s = measurement.system();
          double normApparent = Apparent3Rho.newNormalizedApparent2Rho(s.relativeSystem())
              .value(
                  kw[0], kw[1],
                  hStep / s.lCC(),
                  (int) Math.round(kw[2]), (int) Math.round(kw[3])
              );
          return ValuePair.Name.RHO_1.of(measurement.resistivity() / normApparent,
              0.0
          );
        })
        .reduce(ValuePair::mergeWith).orElseThrow();
  }
}
