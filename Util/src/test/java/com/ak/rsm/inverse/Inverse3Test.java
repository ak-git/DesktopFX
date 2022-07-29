package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Inverse3Test {
  static Stream<Arguments> model() {
    double rho1 = 9.0;
    double rho2 = 1.0;
    double rho3 = 4.0;
    double hStep = 0.105;
    double dH = hStep * 2.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-dH * 4.0)
                    .system4(7.0).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(60, 100),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(dH)
                    .system4(7.0).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(60, 100)
            )
        )
    );
  }

  @ParameterizedTest
  @MethodSource("model")
  @Disabled("ignored com.ak.rsm.inverse.Inverse3Test.testSingle")
  void testSingle(@Nonnull Collection<Collection<DerivativeMeasurement>> ms) {
    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException("L is not equal for all electrode systems %s".formatted(statisticsL));
    }

    double hStep = ms.stream().flatMapToDouble(
        dm -> dm.stream().flatMapToDouble(m -> DoubleStream.of(Math.abs(m.dh())))
    ).min().orElseThrow();

    int pMin = (int) (ms.stream().flatMapToDouble(
        dm -> dm.stream().flatMapToDouble(m -> DoubleStream.of(Math.abs(m.dh())))
    ).max().orElseThrow() / hStep) + 1;

    var dynamicInverses = ms.stream().map(dm -> DynamicInverse.of(dm, hStep)).toList();

    record P(int p1, int p2mp1) {
      P(@Nonnull int[] p) {
        this(p[0], p[1]);
      }
    }

    Function<P, PointValuePair> cache = new ConcurrentCache<>(
        p -> {
          Logger.getLogger(getClass().getName()).info(p::toString);
          return Simplex.optimizeAll(
              kw -> dynamicInverses.stream().mapToDouble(value -> value.applyAsDouble(new double[] {kw[0], kw[1], p.p1, p.p2mp1}))
                  .reduce(StrictMath::hypot).orElseThrow(),
              new Simplex.Bounds(-1.0, 1.0),
              new Simplex.Bounds(-1.0, 1.0),
              new Simplex.Bounds(Metrics.fromMilli(0.01), Metrics.fromMilli(0.3))
          );
        }
    );

    Phenotype<IntegerGene, Double> phenotype = Engine
        .builder(p -> Simplex.optimizeAll(
            kw -> cache.apply(new P(p)).getValue(),
            new Simplex.Bounds(-1.0, 1.0),
            new Simplex.Bounds(-1.0, 1.0)
        ).getValue(), Codecs.ofVector(new IntRange[] {IntRange.of(pMin, 100), IntRange.of(1, 100)}))
        .populationSize(1 << 3)
        .optimize(Optimize.MINIMUM)
        .alterers(new GaussianMutator<>(0.6), new Mutator<>(0.03), new MeanAlterer<>(0.6))
        .build().stream()
        .limit(Limits.bySteadyFitness(3)).limit(100)
        .peek(r -> Logger.getLogger(getClass().getName()).info(() -> r.bestPhenotype().toString()))
        .collect(toBestPhenotype());
    assertNotNull(phenotype);

    Genotype<IntegerGene> best = phenotype.genotype();

    P optimal = new P(IntStream.range(0, best.length()).map(i -> best.get(i).get(0).intValue()).toArray());
    Logger.getLogger(getClass().getName()).info(optimal::toString);

    PointValuePair kwOptimal = cache.apply(optimal);
    double[] kwpp = {kwOptimal.getPoint()[0], kwOptimal.getPoint()[1], optimal.p1, optimal.p2mp1};
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
