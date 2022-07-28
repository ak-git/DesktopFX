package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
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
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4)
                    .system4(7.0).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(60, 100),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(hStep * 2)
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

    Phenotype<IntegerGene, Double> phenotype = Engine
        .builder(p -> Simplex.optimizeAll(
            kw -> dynamicInverses.stream().mapToDouble(value -> value.applyAsDouble(new double[] {kw[0], kw[1], p[0], p[1]}))
                .reduce(StrictMath::hypot).orElseThrow(),
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
    PointValuePair pOptimal = new PointValuePair(
        IntStream.range(0, best.length()).mapToDouble(i -> best.get(i).get(0).doubleValue()).toArray(),
        phenotype.fitness()
    );
    Logger.getLogger(getClass().getName()).info(() -> Arrays.toString(pOptimal.getPoint()));
  }
}
