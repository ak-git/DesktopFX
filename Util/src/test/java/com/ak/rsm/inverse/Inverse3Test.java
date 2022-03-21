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
import io.jenetics.GaussianMutator;
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
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.1).system4(10.0)
                .rho1(9.0).rho2(1.0).rho3(9.0).hStep(0.1).p(50, 50),
        },
    };
  }

  @Test(dataProvider = "single", enabled = false)
  public void testSingle(@Nonnull Collection<? extends DerivativeMeasurement> ms) {
    int pTotal = 100;
    Function<Integer, PointValuePair> cache = new ConcurrentCache<>(
        p1 -> Simplex.optimizeAll(
            kw -> DynamicInverse.of(ms, kw[2]).applyAsDouble(new double[] {kw[0], kw[1], p1, pTotal - p1}),
            new Simplex.Bounds(-1.0, 1.0),
            new Simplex.Bounds(-1.0, 1.0),
            new Simplex.Bounds(Metrics.fromMilli(0.01), Metrics.fromMilli(1.0))
        )
    );

    Phenotype<IntegerGene, Double> phenotype = Engine
        .builder(
            p1 -> cache.apply(p1[0]).getValue(),
            Codecs.ofVector(IntRange.of(1, 99))
        )
        .populationSize(1 << 3)
        .optimize(Optimize.MINIMUM)
        .alterers(new GaussianMutator<>(0.5), new Mutator<>(0.03), new MeanAlterer<>(0.6))
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

    int p1 = (int) pOptimal.getPoint()[0];
    PointValuePair kwOptimal = cache.apply(p1);
    double[] kwpp = {kwOptimal.getPoint()[0], kwOptimal.getPoint()[1], p1, pTotal - p1};
    double hStep = kwOptimal.getPoint()[2];
    var rho1 = getRho1(ms, kwpp, hStep);
    var rho2 = ValuePair.Name.RHO_2.of(rho1.value() / Layers.getRho1ToRho2(kwpp[0]), 0.0);
    var rho3 = ValuePair.Name.RHO_3.of(rho2.value() / Layers.getRho1ToRho2(kwpp[1]), 0.0);
    Logger.getAnonymousLogger().info(
        () -> "%.6f %s; %s; %s; %s; %s; %s; %s".formatted(
            kwOptimal.getValue(), ValuePair.Name.K12.of(kwpp[0], 0.0), ValuePair.Name.K23.of(kwpp[1], 0.0),
            ValuePair.Name.H.of(hStep, 0.0),
            Arrays.stream(kwpp).skip(2).map(p -> p * hStep).mapToObj(h -> ValuePair.Name.H.of(h, 0.0)).toList(),
            rho1, rho2, rho3)
    );
  }

  @DataProvider(name = "noChanged")
  public static Object[][] noChanged() {
    double[] indentationsMilli = {0, -0.5, -1.0};
    return new Object[][] {
        {
            Arrays.stream(indentationsMilli).mapToObj(i ->
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.1).system2(6.0)
                    .rho1(9.0).rho2(1.0).rho3(9.0).hStep(0.1).p(50, 50 + Math.toIntExact(Math.round(i / 0.1)))
            ).toList(),
            indentationsMilli
        },
    };
  }

  @Test(dataProvider = "noChanged", enabled = false)
  @ParametersAreNonnullByDefault
  public void testNoChanged(Collection<Collection<? extends DerivativeMeasurement>> ms, double[] indentationsMilli) {
    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException("L is not equal for all electrode systems %s".formatted(statisticsL));
    }

    double hStep = Metrics.fromMilli(0.1);
    List<ToDoubleFunction<double[]>> dynamicInverses = ms.stream().map(dm -> DynamicInverse.of(dm, hStep)).toList();

    record P(@Nonnegative int p1, @Nonnegative int p2mp1) {
      P(@Nonnull int[] p) {
        this(p[0], p[1]);
      }

      P(@Nonnull double[] p) {
        this((int) Math.round(p[0]), (int) Math.round(p[1]));
      }
    }

    Function<P, PointValuePair> cache = new ConcurrentCache<>(
        p -> Simplex.optimizeAll(
            kw -> {
              Iterator<double[]> iterator = Arrays.stream(indentationsMilli)
                  .mapToObj(
                      x -> new double[] {kw[0], kw[1], p.p1, p.p2mp1 + x / 0.1}
                  )
                  .iterator();
              return dynamicInverses.stream().mapToDouble(value -> value.applyAsDouble(iterator.next()))
                  .reduce(StrictMath::hypot).orElseThrow();
            },
            new Simplex.Bounds(-1.0, 1.0),
            new Simplex.Bounds(-1.0, 1.0)
        )
    );

    Phenotype<IntegerGene, Double> phenotype = Engine
        .builder(
            p -> cache.apply(new P(p)).getValue(),
            Codecs.ofVector(new IntRange[] {IntRange.of(15, 100), IntRange.of(15, 100)})
        )
        .populationSize(1 << 3)
        .optimize(Optimize.MINIMUM)
        .alterers(new GaussianMutator<>(0.5), new Mutator<>(0.03), new MeanAlterer<>(0.6))
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

    int p1 = (int) pOptimal.getPoint()[0];
    int p2mp1 = (int) pOptimal.getPoint()[1];
    PointValuePair kwOptimal = cache.apply(new P(pOptimal.getPoint()));
    double[] kwpp = {kwOptimal.getPoint()[0], kwOptimal.getPoint()[1], p1, p2mp1};

    var rho1 = ms.stream().map(dm -> getRho1(dm, kwpp, hStep)).reduce(ValuePair::mergeWith).orElseThrow();
    var rho2 = ValuePair.Name.RHO_2.of(rho1.value() / Layers.getRho1ToRho2(kwpp[0]), 0.0);
    var rho3 = ValuePair.Name.RHO_3.of(rho2.value() / Layers.getRho1ToRho2(kwpp[1]), 0.0);
    Logger.getAnonymousLogger().info(
        () -> "%.6f %s; %s; %s; %s; %s; %s; %s".formatted(
            kwOptimal.getValue(), ValuePair.Name.K12.of(kwpp[0], 0.0), ValuePair.Name.K23.of(kwpp[1], 0.0),
            ValuePair.Name.H.of(hStep, 0.0),
            Arrays.stream(kwpp).skip(2).map(p -> p * hStep).mapToObj(h -> ValuePair.Name.H.of(h, 0.0)).toList(),
            rho1, rho2, rho3)
    );
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
