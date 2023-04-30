package com.ak.rsm.inverse;

import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class Inverse2Test {
  private static final Logger LOGGER = Logger.getLogger(Inverse2Test.class.getName());

  @ParameterizedTest
  @MethodSource({
      "com.ak.rsm.inverse.InverseTestE7932Provider#meatFat",
      "com.ak.rsm.inverse.InverseTestE7932Provider#meat",
  })
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testAlpha01")
  void testAlpha01(@Nonnull Collection<Collection<DerivativeMeasurement>> ms) {
    testForSystems(ms, Double.POSITIVE_INFINITY, Regularization.Interval.MIN_MAX.of(0.1));
  }

  @ParameterizedTest
  @MethodSource({
      "com.ak.rsm.inverse.InverseTestE7935Provider#meat",
      "com.ak.rsm.inverse.InverseTestE7935Provider#meat2",
      "com.ak.rsm.inverse.InverseTestE7935Provider#fatMeat",
      "com.ak.rsm.inverse.InverseTestE7935Provider#fatMeat2",
  })
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testAlpha02")
  void testAlpha02(@Nonnull Collection<Collection<DerivativeMeasurement>> ms) {
    testForSystems(ms, Double.POSITIVE_INFINITY, Regularization.Interval.MIN_MAX.of(0.2));
  }

  @ParameterizedTest
  @MethodSource({
      "com.ak.rsm.inverse.InverseTestE7940Provider#fatMeat10",
      "com.ak.rsm.inverse.InverseTestE7940Provider#fatMeat08",
      "com.ak.rsm.inverse.InverseTestE7940Provider#fatMeat07",

      "com.ak.rsm.inverse.InverseTestE7956Provider#fatSkinBottom2",
      "com.ak.rsm.inverse.InverseTestE7956Provider#meatFat"
  })
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testAlpha10")
  void testAlpha10(@Nonnull Collection<Collection<DerivativeMeasurement>> ms) {
    testForSystems(ms, Double.POSITIVE_INFINITY, Regularization.Interval.MIN_MAX.of(10.0));
  }

  @ParameterizedTest
  @MethodSource({
      "com.ak.rsm.inverse.InverseTestE8178akProvider#e8178_17_45_08",
  })
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testAlpha1")
  void testAlpha1(@Nonnull Collection<Collection<DerivativeMeasurement>> ms) {
    testForSystems(ms, Double.POSITIVE_INFINITY, Regularization.Interval.ZERO_MAX.of(1.0));
  }

  @ParameterizedTest
  @MethodSource({
      "com.ak.rsm.inverse.InverseTestE8205akProvider#e8205_18_11_27",
      "com.ak.rsm.inverse.InverseTestE8205akProvider#e8205_18_06_48"
  })
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testAlpha2")
  void testAlpha2(@Nonnull Collection<Collection<DerivativeMeasurement>> ms) {
    testForSystems(ms, Metrics.fromMilli(0.3), Regularization.Interval.ZERO_MAX.of(2.0));
  }

  private void testForSystems(@Nonnull Collection<Collection<DerivativeMeasurement>> ms,
                              @Nonnegative double maxDh,
                              @Nonnull Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
    testSingle(ms.stream()
        .filter(dm -> Math.abs(dm.stream().mapToDouble(DerivativeResistivity::dh).summaryStatistics().getAverage()) < maxDh)
        .toList(), regularizationFunction);
  }

  @ParameterizedTest
  @MethodSource("layer2Model")
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testSingle")
  @ParametersAreNonnullByDefault
  void testSingle(Collection<? extends Collection<? extends DerivativeMeasurement>> ms,
                  Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
    var derivativeMeasurements = convert(ms);
    LOGGER.fine(() -> "converted to:%n%s".formatted(derivativeMeasurements.stream().map(Object::toString).collect(Collectors.joining(Strings.NEW_LINE))));
    LOGGER.info(regularizationFunction::toString);
    var medium = new DynamicAbsolute(derivativeMeasurements, regularizationFunction).get();
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
    LOGGER.fine(() -> "initial:%n%s".formatted(ms.stream()
        .map(
            m -> m.stream().map(Object::toString).collect(Collectors.joining(Strings.NEW_LINE)))
        .collect(Collectors.joining(Strings.NEW_LINE_2)))
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
