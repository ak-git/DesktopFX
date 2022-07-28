package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.medium.MediumLayers;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.util.Strings;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Inverse2Test {
  @ParameterizedTest
  @MethodSource({
      "com.ak.rsm.inverse.InverseTestE7701Provider#e7701_14_30_08_s4",
      "com.ak.rsm.inverse.InverseTestE7701Provider#e7701_14_31_24_s4",
      "com.ak.rsm.inverse.InverseTestE7701Provider#e7701_14_33_36_s4"
  })
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testCombinations")
  void testCombinations(@Nonnull List<Collection<DerivativeMeasurement>> ms) {
    IntStream.rangeClosed(1, ms.size())
        .mapToObj(value ->
            StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(CombinatoricsUtils.combinationsIterator(ms.size(), value), Spliterator.ORDERED),
                false)
        )
        .flatMap(Function.identity())
        .filter(ints -> ints.length == ms.size())
        .map(ints -> {
          Logger.getLogger(getClass().getName()).info(() -> Arrays.toString(ints));
          return IntStream.of(ints).mapToObj(ms::get).collect(Collectors.toList());
        })
        .forEach(this::testSingle);
  }

  @ParameterizedTest
  @MethodSource("layer2Model")
  @Disabled("ignored com.ak.rsm.inverse.Inverse2Test.testSingle")
  void testSingle(@Nonnull Collection<Collection<DerivativeMeasurement>> ms) {
    Logger.getLogger(Inverse2Test.class.getName()).fine(
        () -> ms.stream()
            .map(
                m -> m.stream().map(Object::toString).collect(Collectors.joining(Strings.NEW_LINE)))
            .collect(Collectors.joining(Strings.NEW_LINE_2))
    );
    DoubleSummaryStatistics statisticsL = ms.stream().mapToDouble(Measurements::getBaseL).summaryStatistics();
    if (Double.compare(statisticsL.getMax(), statisticsL.getMin()) != 0) {
      throw new IllegalStateException("L is not equal for all electrode systems %s".formatted(statisticsL));
    }
    List<ToDoubleFunction<double[]>> dynamicInverses = ms.stream().map(DynamicInverse::of).toList();

    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> dynamicInverses.stream().mapToDouble(value -> value.applyAsDouble(kw))
            .reduce(StrictMath::hypot).orElseThrow(),
        new Simplex.Bounds(-1.0, 1.0), new Simplex.Bounds(0.0, 1.0)
    );
    assertNotNull(kwOptimal);
    List<Layer2Medium> mediumList = ms.stream().map(dm -> new Layer2Medium(dm, new Layer2RelativeMedium(kwOptimal.getPoint()))).toList();
    var rho1 = mediumList.stream().map(MediumLayers::rho1).reduce(ValuePair::mergeWith).orElseThrow();
    var rho2 = mediumList.stream().map(MediumLayers::rho2).reduce(ValuePair::mergeWith).orElseThrow();
    var h = mediumList.stream().map(MediumLayers::h1).reduce(ValuePair::mergeWith).orElseThrow();
    Logger.getAnonymousLogger().info(() -> "%.6f; %s; %s; %s".formatted(kwOptimal.getValue(), rho1, rho2, h));
  }

  static Stream<Arguments> layer2Model() {
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0).rho1(1.0).rho2(4.0).h(7.0),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0).rho1(1.0).rho2(4.0).h(7.0),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0).rho1(1.0).rho2(4.0).h(7.0),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0).rho1(1.0).rho2(4.0).h(7.0),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0).rho1(1.0).rho2(4.0).h(7.0),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0).rho1(1.0).rho2(4.0).h(7.0)
            )
        )
    );
  }
}
