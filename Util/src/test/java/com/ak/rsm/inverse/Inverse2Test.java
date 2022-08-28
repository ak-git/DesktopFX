package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class Inverse2Test {
  private static final Logger LOGGER = Logger.getLogger(Inverse2Test.class.getName());

  @ParameterizedTest
  @MethodSource({
      "com.ak.rsm.inverse.InverseTestE7645Provider#n18_09_44",
      "com.ak.rsm.inverse.InverseTestE7645Provider#n18_10_42",
      "com.ak.rsm.inverse.InverseTestE7645Provider#n18_12_22",
      "com.ak.rsm.inverse.InverseTestE7645Provider#n18_12_59",

      "com.ak.rsm.inverse.InverseTestE7661Provider#ak14_26_39",

      "com.ak.rsm.inverse.InverseTestE7672Provider#ak16_44_53",

      "com.ak.rsm.inverse.InverseTestE7694Provider#e7696_17_52_54_s4",
      "com.ak.rsm.inverse.InverseTestE7694Provider#e7696_17_55_32_s4",
      "com.ak.rsm.inverse.InverseTestE7694Provider#e7696_17_56_55_s4",
      "com.ak.rsm.inverse.InverseTestE7694Provider#e7696_17_58_47_s4",
      "com.ak.rsm.inverse.InverseTestE7694Provider#e7696_18_23_57_s4",
      "com.ak.rsm.inverse.InverseTestE7694Provider#e7696_18_27_01_s4",

      "com.ak.rsm.inverse.InverseTestE7694Provider#e7696_17_52_54_s4",
      "com.ak.rsm.inverse.InverseTestE7694Provider#e7696_17_55_32_s4",
      "com.ak.rsm.inverse.InverseTestE7694Provider#e7696_17_56_55_s4",
      "com.ak.rsm.inverse.InverseTestE7694Provider#e7696_17_58_47_s4",
      "com.ak.rsm.inverse.InverseTestE7694Provider#e7696_18_23_57_s4",
      "com.ak.rsm.inverse.InverseTestE7694Provider#e7696_18_27_01_s4",

      "com.ak.rsm.inverse.InverseTestE7701Provider#e7701_14_30_08_s4",
      "com.ak.rsm.inverse.InverseTestE7701Provider#e7701_14_31_24_s4",
      "com.ak.rsm.inverse.InverseTestE7701Provider#e7701_14_33_36_s4",

      "com.ak.rsm.inverse.InverseTestE7851Provider#e7851_14_49_05_315",
      "com.ak.rsm.inverse.InverseTestE7851Provider#e7851_14_49_05_420",
      "com.ak.rsm.inverse.InverseTestE7851Provider#e7851_14_49_05_525",
      "com.ak.rsm.inverse.InverseTestE7851Provider#e7851_14_49_05_609",
      "com.ak.rsm.inverse.InverseTestE7851Provider#e7851_14_49_05_735",
      "com.ak.rsm.inverse.InverseTestE7851Provider#e7851_14_49_05_840",
      "com.ak.rsm.inverse.InverseTestE7851Provider#e7851_14_49_05_945",
      "com.ak.rsm.inverse.InverseTestE7851Provider#e7851_14_49_05_1092",
      "com.ak.rsm.inverse.InverseTestE7851Provider#e7851_14_49_05_1197",
      "com.ak.rsm.inverse.InverseTestE7851Provider#e7851_14_49_05_1197a",
      "com.ak.rsm.inverse.InverseTestE7851Provider#e7851_14_49_05_567",
      "com.ak.rsm.inverse.InverseTestE7851Provider#e7851_14_49_05_567a",
      "com.ak.rsm.inverse.InverseTestE7851Provider#e7851_14_49_05_168",

      "com.ak.rsm.inverse.InverseTestE7858Provider#e09_46_50_105a",
      "com.ak.rsm.inverse.InverseTestE7858Provider#e09_46_50_210a",
      "com.ak.rsm.inverse.InverseTestE7858Provider#e09_46_50_315a",
      "com.ak.rsm.inverse.InverseTestE7858Provider#e09_46_50_420a",
      "com.ak.rsm.inverse.InverseTestE7858Provider#e09_46_50_525a",
      "com.ak.rsm.inverse.InverseTestE7858Provider#e09_46_50_630a",
      "com.ak.rsm.inverse.InverseTestE7858Provider#e09_46_50_735",
      "com.ak.rsm.inverse.InverseTestE7858Provider#e09_46_50_630",
      "com.ak.rsm.inverse.InverseTestE7858Provider#e09_46_50_525",
      "com.ak.rsm.inverse.InverseTestE7858Provider#e09_46_50_420",
      "com.ak.rsm.inverse.InverseTestE7858Provider#e09_46_50_315",
      "com.ak.rsm.inverse.InverseTestE7858Provider#e09_46_50_210",
      "com.ak.rsm.inverse.InverseTestE7858Provider#e09_46_50_105",
  })
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testCombinations")
  void testCombinations(@Nonnull List<Collection<DerivativeMeasurement>> ms) {
    testForSystems(ms, 2);
    testForSystems(ms, ms.get(0).size());
  }

  private void testForSystems(@Nonnull List<Collection<DerivativeMeasurement>> ms, @Nonnegative int countSystems) {
    IntToDoubleFunction findDh = i -> ms.get(i).stream().mapToDouble(DerivativeResistivity::dh).summaryStatistics().getAverage();

    IntStream.rangeClosed(1, ms.size())
        .mapToObj(value ->
            StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(CombinatoricsUtils.combinationsIterator(ms.size(), value), Spliterator.ORDERED),
                false)
        )
        .flatMap(Function.identity())
        .filter(ints -> ints.length == ms.size())
        .map(ints -> Arrays.stream(ints).filter(i -> Math.abs(findDh.applyAsDouble(i)) > Metrics.fromMilli(0.2)).toArray())
        .map(ints -> {
          LOGGER.fine(() -> Arrays.stream(ints)
              .mapToDouble(findDh)
              .mapToObj(average -> "%.3f".formatted(Metrics.toMilli(average)))
              .collect(Collectors.joining("; ", "dh = [", "] mm"))
          );
          return IntStream.of(ints).mapToObj(ms::get).collect(Collectors.toList());
        })
        .map(dm -> dm.stream().map(derivativeMeasurements -> derivativeMeasurements.stream().limit(countSystems).toList()).toList())
        .forEach(this::testSingle);
  }

  @ParameterizedTest
  @MethodSource("layer2Model")
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testSingle")
  void testSingle(@Nonnull Collection<? extends Collection<? extends DerivativeMeasurement>> ms) {
    var derivativeMeasurements = convert(ms);
    LOGGER.fine(() -> "%n%s".formatted(derivativeMeasurements.stream().map(Object::toString).collect(Collectors.joining(Strings.NEW_LINE))));
    var medium = new DynamicAbsolute(derivativeMeasurements).get();
    Assertions.assertNotNull(medium);
    LOGGER.info(medium::toString);
  }

  static Stream<Arguments> layer2Model() {
    double rho1 = 4.0;
    double rho2 = 1.0;
    double hmm = 8.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0).rho1(rho1).rho2(rho2).h(hmm),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0).rho1(rho1).rho2(rho2).h(hmm),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0).rho1(rho1).rho2(rho2).h(hmm),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0).rho1(rho1).rho2(rho2).h(hmm),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0).rho1(rho1).rho2(rho2).h(hmm),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0).rho1(rho1).rho2(rho2).h(hmm)
            )
        )
    );
  }

  static Collection<? extends DerivativeMeasurement> convert(@Nonnull Collection<? extends Collection<? extends DerivativeMeasurement>> ms) {
    var firstMeasurements = ms.iterator().next();
    LOGGER.fine(() -> ms.stream()
        .map(
            m -> m.stream().map(Object::toString).collect(Collectors.joining(Strings.NEW_LINE)))
        .collect(Collectors.joining(Strings.NEW_LINE_2))
    );

    if (ms.size() == 1) {
      return firstMeasurements;
    }

    LOGGER.fine(() -> {
      var electrodeSystemsStat = ms.stream().mapToInt(Collection::size).summaryStatistics();
      if (electrodeSystemsStat.getMin() == electrodeSystemsStat.getMax()) {
        var tetrapolarSystems = firstMeasurements.stream().map(Measurement::system).map(TetrapolarSystem::toString)
            .collect(Collectors.joining("; ", "[", "]"));
        return "Use %d electrode systems: %s".formatted(electrodeSystemsStat.getMin(), tetrapolarSystems);
      }
      else {
        throw new IllegalStateException("Count systems is not equal for all electrode systems %s".formatted(electrodeSystemsStat));
      }
    });
    var statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException("L is not equal for all electrode systems %s".formatted(statisticsL));
    }

    var avgDerivate = ms.stream()
        .map(dm -> dm.stream()
            .map(m -> new WeightedObservedPoint(1.0, m.dh(), m.derivativeResistivity())).toList()
        )
        .reduce(
            firstMeasurements.stream().map(dm -> new WeightedObservedPoints()).toList(),
            (wpList, wp) -> {
              IntStream.range(0, wpList.size()).forEach(i -> wpList.get(i).add(wp.get(i)));
              return wpList;
            },
            (eq1, eq2) -> eq1)
        .stream()
        .map(weightedObservedPoints -> PolynomialCurveFitter.create(2).fit(weightedObservedPoints.toList()))
        .mapToDouble(value -> value[0])
        .iterator();

    return firstMeasurements.stream()
        .map(dm -> TetrapolarDerivativeMeasurement
            .ofSI(dm.inexact().absError()).dh(Double.NaN)
            .system(dm.inexact().system().sPU(), dm.inexact().system().lCC())
            .rho(dm.resistivity(), avgDerivate.nextDouble()))
        .toList();
  }
}
