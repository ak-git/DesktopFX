package com.ak.appliance.rcm.comm.converter;

import com.ak.appliance.aper.comm.converter.SplineCoefficientsUtils;
import com.ak.appliance.rcm.numbers.RcmBaseSurfaceCoefficientsChannel1;
import com.ak.appliance.rcm.numbers.RcmBaseSurfaceCoefficientsChannel2;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.Variable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import java.nio.ByteOrder;
import java.util.stream.Stream;

import static com.ak.appliance.rcm.comm.converter.RcmOutVariable.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RcmConverterTest {
  static Stream<Arguments> variables() {
    return Stream.of(
        arguments(
            new byte[] {-10, -36, -125, -72, -5, -60, -125, -124, -111, -94, -7, -98, -127, -128, -5, -78, -127, -10, -127, -128},
            new int[] {-67590, 5493, 0, 66, -38791, 31534, 0}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("variables")
  void testApply(byte[] inputBytes, int[] outputInts) {
    Converter<BufferFrame, RcmOutVariable> converter = LinkedConverter.of(new RcmConverter(), RcmOutVariable.class);
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 759; i++) {
      long count = converter.apply(bufferFrame).count();
      assertThat(count).withFailMessage("Set cycles to %d", i).isZero();
    }
    assertThat(converter.apply(bufferFrame)).withFailMessage("Increase cycles!").hasSize(5).startsWith(outputInts);
    assertThat(converter.getFrequency()).isEqualTo(200.0);
  }

  @ParameterizedTest
  @EnumSource(value = RcmInVariable.class, names = {"RHEO_1X", "RHEO_2X", "ECG_X"})
  void testX(Variable<RcmInVariable> variable) {
    assertThat(variable.options()).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(value = RcmOutVariable.class)
  void testInputVariablesClass(DependentVariable<RcmInVariable, RcmOutVariable> variable) {
    assertThat(variable.getInputVariablesClass()).isEqualTo(RcmInVariable.class);
  }

  @ParameterizedTest
  @EnumSource(value = RcmOutVariable.class, names = {"RHEO_1", "RHEO_2"})
  void testRheoChannels(Variable<RcmOutVariable> variable) {
    assertAll(variable.name(),
        () -> assertThat(variable.getUnit()).isEqualTo(MetricPrefix.MICRO(Units.OHM)),
        () -> assertThat(variable.options()).contains(Variable.Option.INVERSE)
    );
  }

  @ParameterizedTest
  @EnumSource(value = RcmOutVariable.class, names = {"RHEO_1", "RHEO_2", "ECG"})
  void testZeroInRange(Variable<RcmOutVariable> variable) {
    assertThat(variable.options()).contains(Variable.Option.FORCE_ZERO_IN_RANGE);
  }

  @ParameterizedTest
  @EnumSource(value = RcmOutVariable.class, names = {"BASE_1", "BASE_2"})
  void testBaseChannels(Variable<RcmOutVariable> variable) {
    assertThat(variable.getUnit()).isEqualTo(MetricPrefix.MILLI(Units.OHM));
  }

  @ParameterizedTest
  @EnumSource(value = RcmOutVariable.class, names = {"QS_1", "QS_2"})
  void testQoSChannels(Variable<RcmOutVariable> variable) {
    assertAll(variable.name(),
        () -> assertThat(variable.getUnit()).isEqualTo(Units.OHM),
        () -> assertThat(variable.options()).contains(Variable.Option.TEXT_VALUE_BANNER)
    );
  }

  @ParameterizedTest
  @EnumSource(value = RcmOutVariable.class, names = {"ECG"})
  void testECGChannel(Variable<RcmOutVariable> variable) {
    assertThat(variable.getUnit()).isEqualTo(MetricPrefix.MILLI(Units.VOLT));
  }

  static Stream<Arguments> filterDelay() {
    return Stream.of(
        arguments(RHEO_1, 3.5),
        arguments(BASE_1, 377.0),
        arguments(QS_1, 3.5),
        arguments(ECG, 3.5),
        arguments(RHEO_2, 3.5),
        arguments(BASE_2, 377.0),
        arguments(QS_2, 3.5)
    );
  }

  @ParameterizedTest
  @MethodSource("filterDelay")
  void testFilterDelay(Variable<RcmOutVariable> variable, double delay) {
    assertThat(variable.filter().getDelay()).isEqualTo(delay);
  }

  @Test
  @Disabled("ignored com.ak.appliance.converter.comm.rcm.RcmConverterTest.testBaseSplineSurface1")
  void testBaseSplineSurface1() {
    SplineCoefficientsUtils.testSplineSurface(RcmBaseSurfaceCoefficientsChannel1.class);
    SplineCoefficientsUtils.testSplineSurface(RcmBaseSurfaceCoefficientsChannel2.class);
  }
}