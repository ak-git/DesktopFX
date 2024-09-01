package com.ak.rsm.inverse;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import com.ak.util.Metrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tec.uom.se.unit.Units.METRE;

class Inverse2StaticTest {
  static Stream<Arguments> relativeRiseErrors() {
    double absErrorMilli = 0.001;
    double hmm = 15.0;
    return Stream.of(
        arguments(
            TetrapolarMeasurement.milli(absErrorMilli).system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm),
            new double[] {136.7, 31.0}
        ),
        arguments(
            TetrapolarMeasurement.milli(-absErrorMilli).withShiftError().system2(10.0).ofOhms(
                TetrapolarResistance.milli().system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm)
                    .stream().mapToDouble(Resistance::ohms).toArray()
            ),
            new double[] {138.7, 31.2}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("relativeRiseErrors")
  void testInverseRelativeRiseErrors(Collection<? extends Measurement> measurements, double[] riseErrors) {
    double absError = measurements.stream().mapToDouble(m -> m.inexact().absError()).average().orElseThrow();
    double baseL = Resistivity.getBaseL(measurements);
    double dim = measurements.stream().mapToDouble(m -> m.system().getDim()).max().orElseThrow();

    var medium = Relative.Static.solve(measurements);
    assertAll(medium.toString(),
        () -> assertThat(medium.k().absError() / (absError / dim)).isCloseTo(riseErrors[0], byLessThan(0.1)),
        () -> assertThat(medium.hToL().absError() / (absError / baseL)).isCloseTo(riseErrors[1], byLessThan(0.1))
    );
  }

  static Stream<Arguments> relative() {
    double absErrorMilli = 0.001;
    double hmm = 15.0 / 2.0;
    double rho1 = 1.0;
    double k = 0.6;
    double rho2 = rho1 / Layers.getRho1ToRho2(k);
    return Stream.of(
        arguments(
            TetrapolarMeasurement.milli(absErrorMilli).system2(10.0).rho1(1.0).rho2(rho2).h(hmm),
            new RelativeMediumLayers(
                ValuePair.Name.K12.of(0.6, 0.0010),
                ValuePair.Name.H_L.of(0.25, 0.00039)
            )
        ),
        arguments(
            TetrapolarMeasurement.milli(-absErrorMilli).withShiftError().system2(10.0).ofOhms(
                TetrapolarResistance.milli().system2(10.0).rho1(rho1).rho2(rho2).h(hmm)
                    .stream().mapToDouble(Resistance::ohms).toArray()
            ),
            new RelativeMediumLayers(
                ValuePair.Name.K12.of(0.599, 0.0010),
                ValuePair.Name.H_L.of(0.2496, 0.00039)
            )
        )
    );
  }

  @ParameterizedTest
  @MethodSource("relative")
  void testInverseRelative(Collection<? extends Measurement> measurements, RelativeMediumLayers expected) {
    var medium = Relative.Static.solve(measurements);
    assertAll(medium.toString(),
        () -> assertThat(medium.k().value()).isCloseTo(expected.k().value(), byLessThan(expected.k().absError())),
        () -> assertThat(medium.k().absError()).isCloseTo(expected.k().absError(), withinPercentage(10.0)),
        () -> assertThat(medium.hToL().value()).isCloseTo(expected.hToL().value(), byLessThan(expected.hToL().absError())),
        () -> assertThat(medium.hToL().absError()).isCloseTo(expected.hToL().absError(), withinPercentage(10.0))
    );
  }

  static Stream<Arguments> absolute() {
    double absErrorMilli = 0.001;
    double hmm = 15.0 / 2;
    return Stream.of(
        // system 1
        arguments(
            List.of(TetrapolarMeasurement.ofMilli(0.1).system(10.0, 20.0).rho(9.0)),
            new ValuePair[] {
                ValuePair.Name.RHO.of(9.0, 0.27),
                ValuePair.Name.RHO_1.of(9.0, 0.27),
                ValuePair.Name.RHO_2.of(9.0, 0.27),
                ValuePair.Name.H.of(Double.NaN, Double.NaN)
            }
        ),
        // system 4 gets fewer errors
        arguments(
            TetrapolarMeasurement.milli(absErrorMilli).system4(10.0).rho1(1.0).rho2(4.0).h(hmm),
            new ValuePair[] {
                ValuePair.Name.RHO.of(1.6267, 0.00012),
                ValuePair.Name.RHO_1.of(1.0, 0.00073),
                ValuePair.Name.RHO_2.of(4.0, 0.012),
                ValuePair.Name.H.of(Metrics.Length.MILLI.to(hmm, METRE), Metrics.Length.MILLI.to(0.0098, METRE))
            }
        ),
        // system 2 gets more errors
        arguments(
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(Double.NaN).system2(10.0)
                .rho(1.4441429093546185, 1.6676102911913226, -3.0215753166196184, -3.49269170918376),
            new ValuePair[] {
                ValuePair.Name.RHO.of(1.5845, 0.00018),
                ValuePair.Name.RHO_1.of(1.0, 0.0011),
                ValuePair.Name.RHO_2.of(4.0, 0.017),
                ValuePair.Name.H.of(Metrics.Length.MILLI.to(hmm, METRE), Metrics.Length.MILLI.to(0.012, METRE))
            }
        )
    );
  }

  @ParameterizedTest
  @MethodSource("absolute")
  void testInverseAbsolute(Collection<? extends Measurement> measurements, ValuePair[] expected) {
    var medium = StaticAbsolute.LAYER_2.apply(measurements);
    assertAll(medium.toString(),
        () -> assertThat(medium.rho()).isEqualTo(expected[0]),
        () -> assertThat(medium.rho1()).isEqualTo(expected[1]),
        () -> assertThat(medium.rho2()).isEqualTo(expected[2]),
        () -> assertThat(medium.h()).isEqualTo(expected[3])
    );
  }

  @Test
  void testEmptyMeasurements() {
    assertThatException()
        .isThrownBy(() -> StaticAbsolute.LAYER_2.apply(Collections.emptyList()))
        .withMessage("No value present");
  }
}
