package com.ak.rsm.inverse;

import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent3Rho;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.ConcurrentCache;
import com.ak.util.Metrics;
import com.ak.util.Numbers;
import io.jenetics.*;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.Limits;
import io.jenetics.util.BaseSeq;
import io.jenetics.util.IntRange;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.*;

import static io.jenetics.engine.EvolutionResult.toBestGenotype;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Inverse3Test {
  private static final Logger LOGGER = Logger.getLogger(Inverse3Test.class.getName());

  static Stream<Arguments> model() {
    double rho1 = 9.0;
    double rho2 = 1.0;
    double rho3 = 4.0;
    double hmmStep = 0.205;
    double smmBase = 8.0;
    int p1 = (int) (2.0 / hmmStep);
    int p2mp1 = (int) (4.3 / hmmStep);
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-hmmStep * 3.0)
                    .system4(smmBase).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hmmStep).p(p1, p2mp1),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(hmmStep)
                    .system4(smmBase).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hmmStep).p(p1, p2mp1),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(hmmStep * 2.0)
                    .system4(smmBase).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hmmStep).p(p1, p2mp1)
            )
        )
    );
  }

  @ParameterizedTest
  @MethodSource("model")
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testCombinations")
  void testCombinations(@Nonnull List<Collection<DerivativeMeasurement>> ms) {
    IntToDoubleFunction findDh = i -> ms.get(i).stream().mapToDouble(DerivativeResistivity::dh).summaryStatistics().getAverage();

    IntStream.rangeClosed(1, ms.size())
        .mapToObj(value ->
            StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(CombinatoricsUtils.combinationsIterator(ms.size(), value), Spliterator.ORDERED),
                false)
        )
        .flatMap(Function.identity())
        .filter(ints -> ints.length == ms.size())
        .map(ints -> Arrays.stream(ints)
            .filter(i -> findDh.applyAsDouble(i) > Metrics.fromMilli(0.0))
            .filter(i -> findDh.applyAsDouble(i) < Metrics.fromMilli(0.3))
            .toArray())
        .map(ints -> {
          LOGGER.info(() -> Arrays.stream(ints)
              .mapToDouble(findDh)
              .mapToObj(average -> "%.3f".formatted(Metrics.toMilli(average)))
              .collect(Collectors.joining("; ", "dh = [", "] mm"))
          );
          return IntStream.of(ints).mapToObj(ms::get).collect(Collectors.toList());
        })
        .map(dm -> dm.stream().map(derivativeMeasurements -> derivativeMeasurements.stream().toList()).toList())
        .forEach(this::testSingle);
  }

  @Nonnegative
  @ParametersAreNonnullByDefault
  private static ValuePair getRho1(Collection<? extends DerivativeMeasurement> measurements, double[] kw, @Nonnegative double hStep) {
    return measurements.stream()
        .map(measurement -> {
          TetrapolarSystem s = measurement.system();
          double normApparent = Apparent3Rho.newApparentDivRho1(s.relativeSystem())
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

  @ParameterizedTest
  @MethodSource("model")
  @Disabled("ignored com.ak.rsm.inverse.Inverse3Test.testSingle")
  void testSingle(@Nonnull Collection<? extends Collection<? extends DerivativeMeasurement>> ms) {
    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException("L is not equal for all electrode systems %s".formatted(statisticsL));
    }

    double hStep = ms.stream().flatMapToDouble(
        dm -> dm.stream().flatMapToDouble(m -> DoubleStream.of(Math.abs(m.dh())))
    ).min().orElseThrow() / 2.0;

    int P1 = 50;
    int P2mP1 = 50;
    LOGGER.info(() -> "h = [%.2f; %.2f] mm".formatted(Metrics.toMilli(P1 * hStep), Metrics.toMilli(P2mP1 * hStep)));

    var dynamicInverses = ms.stream().map(dm -> DynamicInverse.of(dm, hStep)).toList();

    record P(int k1, int k2, int p1, int p2mp1) {
      private static final int[] Q149 = {1, 1};
      private static final int[] Q491 = {1, -1};
      private static final int[] Q941 = {-1, -1};
      private static final int[] Q914 = {-1, 1};
      private static final int[] Q = Q914;
      private static final int K_SIZE = 200;
      private static final double[] K_VALUES = Numbers.rangeLog(1.0 / K_SIZE, K_SIZE, K_SIZE + 1).toArray();

      P(@Nonnull int[] v) {
        this(v[0], v[1], v[2], v[3]);
      }

      P(@Nonnull BaseSeq<Chromosome<IntegerGene>> genotype) {
        this(IntStream.range(0, genotype.length()).map(i -> genotype.get(i).get(0).intValue()).toArray());
      }

      double k12() {
        return Q[0] * (1.0 - K_VALUES[k1] / K_SIZE);
      }

      double k23() {
        return Q[1] * (1.0 - K_VALUES[k2] / K_SIZE);
      }
    }

    Function<P, Double> cache = new ConcurrentCache<>(
        p -> {
          if (p.p1 > p.p2mp1) {
            return Double.POSITIVE_INFINITY;
          }
          else {
            return dynamicInverses.stream()
                .mapToDouble(value -> value.applyAsDouble(new double[] {p.k12(), p.k23(), p.p1, p.p2mp1}))
                .reduce(StrictMath::hypot).orElseThrow();
          }
        }
    );

    Consumer<P> print = p -> {
      double[] kwpp = {p.k12(), p.k23(), p.p1, p.p2mp1};
      var rho1 = ms.stream().map(dm -> getRho1(dm, kwpp, hStep)).reduce(ValuePair::mergeWith).orElseThrow();
      var rho2 = ValuePair.Name.RHO_2.of(rho1.value() / Layers.getRho1ToRho2(kwpp[0]), 0.0);
      var rho3 = ValuePair.Name.RHO_3.of(rho2.value() / Layers.getRho1ToRho2(kwpp[1]), 0.0);
      LOGGER.info(
          () -> "%.6f %s; %s; %s; %s; %s; %s; %s".formatted(
              cache.apply(p), ValuePair.Name.K12.of(kwpp[0], 0.0), ValuePair.Name.K23.of(kwpp[1], 0.0),
              ValuePair.Name.H.of(hStep, 0.0),
              Arrays.stream(kwpp).skip(2).map(v -> v * hStep).mapToObj(h -> ValuePair.Name.H.of(h, 0.0)).toList(),
              rho1, rho2, rho3)
      );
    };

    var genotype = Engine
        .builder(v -> cache.apply(new P(v)),
            Codecs.ofVector(IntRange.of(0, P.K_SIZE), IntRange.of(0, P.K_SIZE), IntRange.of(1, P1), IntRange.of(1, P2mP1)))
        .populationSize(1 << 12)
        .optimize(Optimize.MINIMUM)
        .alterers(new GaussianMutator<>(0.6), new Mutator<>(0.03), new MeanAlterer<>(0.6))
        .build().stream()
        .limit(Limits.bySteadyFitness(7)).limit(100)
        .peek(r -> print.accept(new P(r.bestPhenotype().genotype())))
        .collect(toBestGenotype());
    Assertions.assertNotNull(genotype);
    print.accept(new P(genotype));
  }
}
