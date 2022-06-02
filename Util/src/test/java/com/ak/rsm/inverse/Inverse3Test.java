package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent3Rho;
import com.ak.rsm.measurement.DerivativeMeasurement;
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
  @DataProvider(name = "fixedP")
  public static Object[][] fixedP() {
    int p1 = 25;
    int p2mp1 = 130;
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                .ofOhms(122.3, 199.0, 66.0 * 2, 202.0 * 2 - 66.0 * 2,
                122.3 + 0.1, 199.0 + 0.4, (66.0 + 0.1) * 2, (202.0 + 0.25) * 2 - (66.0 + 0.1) * 2),
            p1, p2mp1
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                .ofOhms(122.3, 199.0, 66.0 * 2, 202.0 * 2 - 66.0 * 2,
                122.3 + 0.3, 199.0 + 0.75, (66.0 + 0.2) * 2, (202.0 + 0.75) * 2 - (66.0 + 0.2) * 2),
            p1, p2mp1
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                .ofOhms(122.3, 199.0, 66.0 * 2, 202.0 * 2 - 66.0 * 2,
                122.3 + 0.6, 199.0 + 2.0, (66.0 + 0.6) * 2, (202.0 + 1.75) * 2 - (66.0 + 0.6) * 2),
            p1, p2mp1
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                .ofOhms(122.3, 199.0, 66.0 * 2, 202.0 * 2 - 66.0 * 2,
                122.3 - 0.3, 199.0 - 1.5, (66.0 - 0.3) * 2, (202.0 - 1.0) * 2 - (66.0 - 0.3) * 2),
            p1, p2mp1
        },
    };
  }

  @Test(dataProvider = "fixedP", enabled = false)
  public void testFixed(@Nonnull Collection<? extends DerivativeMeasurement> ms, @Nonnegative int p1, @Nonnegative int p2mp1) {
    double hStep = Metrics.fromMilli(0.1);

    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> DynamicInverse.of(ms, hStep).applyAsDouble(new double[] {kw[0], kw[1], p1, p2mp1}),
        new Simplex.Bounds(-1.0, 1.0),
        new Simplex.Bounds(-1.0, 1.0)
    );

    double[] kwpp = {kwOptimal.getPoint()[0], kwOptimal.getPoint()[1], p1, p2mp1};

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

  @DataProvider(name = "single")
  public static Object[][] single() {
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.1).system4(10.0)
                .rho1(9.0).rho2(1.0).rho3(9.0).hStep(0.1).p(11, 10),
            11 + 10
        },
    };
  }

  @Test(dataProvider = "single", enabled = false)
  public void testSingle1(@Nonnull Collection<? extends DerivativeMeasurement> ms, @Nonnegative int pTotal) {
    Function<Integer, PointValuePair> cache = new ConcurrentCache<>(
        p1 -> Simplex.optimizeAll(
            kw -> DynamicInverse.of(ms, kw[2]).applyAsDouble(new double[] {kw[0], kw[1], p1, pTotal - p1}),
            new Simplex.Bounds(-1.0, 1.0),
            new Simplex.Bounds(-1.0, 1.0),
            new Simplex.Bounds(Metrics.fromMilli(0.01), Metrics.fromMilli(0.3))
        )
    );

    Phenotype<IntegerGene, Double> phenotype = Engine
        .builder(p1 -> cache.apply(p1[0]).getValue(), Codecs.ofVector(IntRange.of(1, pTotal - 1)))
        .populationSize(1 << 3)
        .optimize(Optimize.MINIMUM)
        .alterers(new GaussianMutator<>(0.6), new Mutator<>(0.03), new MeanAlterer<>(0.6))
        .build().stream()
        .limit(Limits.bySteadyFitness(3)).limit(100)
        .peek(r -> extracted(ms, pTotal, cache, r.bestPhenotype()))
        .collect(toBestPhenotype());

    extracted(ms, pTotal, cache, phenotype);
  }

  private static void extracted(Collection<? extends DerivativeMeasurement> ms, int pTotal,
                                Function<Integer, PointValuePair> cache, Phenotype<IntegerGene, Double> phenotype) {
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
  public void testSingle2(@Nonnull Collection<? extends DerivativeMeasurement> ms, @Nonnegative int pTotal) {
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
        .limit(Limits.bySteadyFitness(3)).limit(100)
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
