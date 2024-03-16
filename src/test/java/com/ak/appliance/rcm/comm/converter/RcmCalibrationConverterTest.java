package com.ak.appliance.rcm.comm.converter;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.Variable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RcmCalibrationConverterTest {
  static Stream<Arguments> calibrableVariables() {
    return Stream.of(
        arguments(
            new byte[] {-10, -36, -125, -72, -5, -60, -125, -124, -111, -94, -7, -98, -127, -128, -5, -78, -127, -10, -127, -128},
            new int[] {0, 92, -274, -274, 0}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("calibrableVariables")
  void testApplyCalibrator(byte[] inputBytes, int[] outputInts) {
    Converter<BufferFrame, RcmCalibrationVariable> converter = LinkedConverter.of(new RcmConverter(), RcmCalibrationVariable.class);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 800 - 1; i++) {
      long count = converter.apply(bufferFrame).count();
      assertThat(count).withFailMessage("index %d, count %d".formatted(i, count)).isBetween(0L, 1L);
    }
    assertThat(converter.apply(bufferFrame).peek(ints -> {
      assertThat(ints)
          .withFailMessage(() -> "expected = %s, actual = %s".formatted(Arrays.toString(outputInts), Arrays.toString(ints)))
          .containsExactly(outputInts);
      processed.set(true);
    }).count()).isEqualTo(1);
    assertTrue(processed.get(), "Data are not converted!");
    assertThat(converter.getFrequency()).isEqualTo(200.0);
  }

  @ParameterizedTest
  @EnumSource(value = RcmCalibrationVariable.class)
  void testInputVariablesClass(DependentVariable<RcmInVariable, RcmCalibrationVariable> variable) {
    assertThat(variable.getInputVariablesClass()).isEqualTo(RcmInVariable.class);
  }

  @ParameterizedTest
  @EnumSource(value = RcmCalibrationVariable.class, names = {"CC_ADC", "BASE_ADC", "RHEO_ADC"})
  void testOptions(Variable<RcmCalibrationVariable> variable) {
    assertThat(variable.options()).contains(Variable.Option.VISIBLE);
  }

  @ParameterizedTest
  @EnumSource(value = RcmCalibrationVariable.class, names = {"MIN_RHEO_ADC", "AVG_RHEO_ADC"})
  void testOptions2(Variable<RcmCalibrationVariable> variable) {
    assertThat(variable.options()).contains(Variable.Option.TEXT_VALUE_BANNER);
  }
}