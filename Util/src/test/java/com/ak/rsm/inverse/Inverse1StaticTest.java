package com.ak.rsm.inverse;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import com.ak.rsm.resistance.DeltaH;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Inverse1StaticTest {
  static Stream<Arguments> absolute() {
    double absErrorMilli = 0.001;
    double hmm = 15.0 / 2;
    return Stream.of(
        // system 1
        arguments(
            List.of(TetrapolarMeasurement.ofMilli(0.1).system(10.0, 20.0).rho(9.0)),
            ValuePair.Name.RHO.of(9.0, 0.27)
        ),
        // system 4 gets fewer errors
        arguments(
            TetrapolarMeasurement.milli(absErrorMilli).system4(10.0).rho1(1.0).rho2(4.0).h(hmm),
            ValuePair.Name.RHO.of(1.6267, 0.00012)
        ),
        // system 2 gets more errors
        arguments(
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(DeltaH.NULL).system2(10.0)
                .rho(1.4441429093546185, 1.6676102911913226, -3.0215753166196184, -3.49269170918376),
            ValuePair.Name.RHO.of(1.5845, 0.00018)
        )
    );
  }

  @ParameterizedTest
  @MethodSource("absolute")
  void testInverseAbsoluteLayer1(Collection<? extends Measurement> measurements, ValuePair expected) {
    var medium = StaticAbsolute.LAYER_1.apply(measurements);
    assertThat(medium.rho()).withFailMessage(medium.toString()).isEqualTo(expected);
  }

  @Test
  void testEmptyMeasurements() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> StaticAbsolute.LAYER_1.apply(Collections.emptyList()))
        .withMessage("Empty measurements");
  }
}
