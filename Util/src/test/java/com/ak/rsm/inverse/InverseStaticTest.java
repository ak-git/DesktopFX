package com.ak.rsm.inverse;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseStaticTest {
  private static final Logger LOGGER = Logger.getLogger(InverseStaticTest.class.getName());

  static Stream<Arguments> relativeStaticLayer2RiseErrors() {
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
  @MethodSource("relativeStaticLayer2RiseErrors")
  @ParametersAreNonnullByDefault
  void testInverseRelativeStaticLayer2RiseErrors(Collection<? extends Measurement> measurements, double[] riseErrors) {
    double absError = measurements.stream().mapToDouble(m -> m.inexact().absError()).average().orElseThrow();
    double L = Resistivity.getBaseL(measurements);
    double dim = measurements.stream().mapToDouble(m -> m.system().getDim()).max().orElseThrow();

    var medium = new StaticRelative(measurements).get();
    assertAll(medium.toString(),
        () -> assertThat(medium.k12AbsError() / (absError / dim)).isCloseTo(riseErrors[0], byLessThan(0.1)),
        () -> assertThat(medium.hToLAbsError() / (absError / L)).isCloseTo(riseErrors[1], byLessThan(0.1))
    );
    LOGGER.info(medium::toString);
  }

  static Stream<Arguments> relativeStaticLayer2() {
    double absErrorMilli = 0.001;
    double hmm = 15.0 / 2.0;
    double rho1 = 1.0;
    double k = 0.6;
    double rho2 = rho1 / Layers.getRho1ToRho2(k);
    return Stream.of(
        arguments(
            TetrapolarMeasurement.milli(absErrorMilli).system2(10.0).rho1(1.0).rho2(rho2).h(hmm),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(0.6, 0.0010),
                ValuePair.Name.H_L.of(0.25, 0.00039)
            )
        ),
        arguments(
            TetrapolarMeasurement.milli(-absErrorMilli).withShiftError().system2(10.0).ofOhms(
                TetrapolarResistance.milli().system2(10.0).rho1(rho1).rho2(rho2).h(hmm)
                    .stream().mapToDouble(Resistance::ohms).toArray()
            ),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(0.599, 0.0010),
                ValuePair.Name.H_L.of(0.2496, 0.00039)
            )
        )
    );
  }

  @ParameterizedTest
  @MethodSource("relativeStaticLayer2")
  @ParametersAreNonnullByDefault
  void testInverseRelativeStaticLayer2(Collection<? extends Measurement> measurements, RelativeMediumLayers expected) {
    var medium = new StaticRelative(measurements).get();
    assertAll(medium.toString(),
        () -> assertThat(medium.k12()).isCloseTo(expected.k12(), byLessThan(expected.k12AbsError())),
        () -> assertThat(medium.k12AbsError()).isCloseTo(expected.k12AbsError(), withinPercentage(10.0)),
        () -> assertThat(medium.hToL()).isCloseTo(expected.hToL(), byLessThan(expected.hToLAbsError())),
        () -> assertThat(medium.hToLAbsError()).isCloseTo(expected.hToLAbsError(), withinPercentage(10.0))
    );
    LOGGER.info(medium::toString);
  }
}
