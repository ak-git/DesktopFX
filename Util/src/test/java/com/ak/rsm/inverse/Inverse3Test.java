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

  /**
   * INFO: [[[85]]] -> 4.011052465326599E-5
   * мар. 21, 2022 7:42:14 PM com.ak.rsm.inverse.Inverse3Test testSingle
   * INFO: 0,000040 k₁₂ = -0,958656; k₂₃ = 0,958173; h = 0,061337 mm; [h = 5,213646 mm, h = 0,920055 mm]; ρ₁ = 8,948260 Ω·m; ρ₂ = 0,188882 Ω·m; ρ₃ = 8,842674 Ω·m
   * <p>
   * INFO: [[[50]]] -> 2.698298567035242E-11
   * мар. 21, 2022 7:52:31 PM com.ak.rsm.inverse.Inverse3Test testSingle
   * INFO: 0,000000 k₁₂ = -0,800000; k₂₃ = 0,800000; h = 0,100000 mm; [h = 5,000000 mm, h = 5,000000 mm]; ρ₁ = 9,000000 Ω·m; ρ₂ = 1,000000 Ω·m; ρ₃ = 9,000000 Ω·m
   */
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
        .builder(p1 -> cache.apply(p1[0]).getValue(), Codecs.ofVector(IntRange.of(1, 99)))
        .populationSize(1 << 3)
        .optimize(Optimize.MINIMUM)
        .alterers(new GaussianMutator<>(0.6), new Mutator<>(0.03), new MeanAlterer<>(0.6))
        .build().stream()
        .limit(Limits.bySteadyFitness(7)).limit(100)
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

  @Test(dataProvider = "single", enabled = false)
  public void testSingle2(@Nonnull Collection<? extends DerivativeMeasurement> ms) {
    int pTotal = 100;
    double hStep = Metrics.fromMilli(0.1);
    Function<Integer, PointValuePair> cache = new ConcurrentCache<>(
        p1 -> Simplex.optimizeAll(
            kw -> DynamicInverse.of(ms, hStep).applyAsDouble(new double[] {kw[0], kw[1], p1, pTotal - p1}),
            new Simplex.Bounds(-1.0, 1.0),
            new Simplex.Bounds(-1.0, 1.0)
        )
    );

    Phenotype<IntegerGene, Double> phenotype = Engine
        .builder(p1 -> cache.apply(p1[0]).getValue(), Codecs.ofVector(IntRange.of(1, pTotal - 1)))
        .populationSize(1 << 3)
        .optimize(Optimize.MINIMUM)
        .alterers(new GaussianMutator<>(0.6), new Mutator<>(0.03), new MeanAlterer<>(0.6))
        .build().stream()
        .limit(Limits.bySteadyFitness(7)).limit(100)
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
            indentationsMilli,
            100
        },
//       2021-10-22
        {
            List.of(
//                189.75, 0,
                TetrapolarDerivativeMeasurement.milli(0.1).dh(Double.NaN).system2(6.0)
                    .rho(
                        4.36561500845104, 4.49418992805016, -0.178420874720668, -0.183605798682716
                    ),
//                199.75, -2.1,
                TetrapolarDerivativeMeasurement.milli(0.1).dh(Double.NaN).system2(6.0)
                    .rho(
                        4.37968045714554, 4.51061464886706, -0.244006351213963, -0.29392479021624
                    )
            ),
            new double[] {0, -2.1},
            34
        },
    };
  }

  @Test(dataProvider = "noChanged", enabled = false)
  @ParametersAreNonnullByDefault
  public void testNoChanged(Collection<Collection<? extends DerivativeMeasurement>> ms, double[] indentationsMilli, @Nonnegative int pTotal) {
    if (ms.size() != indentationsMilli.length) {
      throw new IllegalArgumentException(Arrays.toString(indentationsMilli));
    }
    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException("L is not equal for all electrode systems %s".formatted(statisticsL));
    }

    double hSI = Metrics.fromMilli(0.1);
    Function<Integer, PointValuePair> cache = new ConcurrentCache<>(
        p1 -> Simplex.optimizeAll(
            kw -> {
              double hStep = kw[2];
              List<ToDoubleFunction<double[]>> dynamicInverses = ms.stream().map(dm -> DynamicInverse.of(dm, hStep)).toList();
              Iterator<double[]> iterator = Arrays.stream(indentationsMilli)
                  .mapToObj(
                      x -> new double[] {kw[0], kw[1], p1, pTotal - p1 + Metrics.fromMilli(x) / hStep}
                  )
                  .iterator();
              return dynamicInverses.stream().mapToDouble(value -> value.applyAsDouble(iterator.next()))
                  .reduce(StrictMath::hypot).orElseThrow();
            },
            new Simplex.Bounds(-1.0, 1.0),
            new Simplex.Bounds(-1.0, 1.0),
            new Simplex.Bounds(hSI * (pTotal - 1) / pTotal, hSI, hSI * (pTotal + 1) / pTotal)
        )
    );

    int upperShift = Arrays.stream(indentationsMilli).filter(value -> value < 0)
        .map(x -> Metrics.fromMilli(x) / Metrics.fromMilli(0.09)).mapToLong(Math::round).mapToInt(Math::toIntExact).min().orElse(0);

    Phenotype<IntegerGene, Double> phenotype = Engine
        .builder(p1 -> cache.apply(p1[0]).getValue(), Codecs.ofVector(IntRange.of(1, pTotal - 1 + upperShift)))
        .populationSize(1 << 3)
        .optimize(Optimize.MINIMUM)
        .alterers(new GaussianMutator<>(0.6), new Mutator<>(0.03), new MeanAlterer<>(0.6))
        .build().stream()
        .limit(Limits.bySteadyFitness(7)).limit(100)
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
