package com.ak.comm.converter.aper;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.AbstractUnit;

import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AperCalibrationCurrent1VariableTest {
  static Stream<Arguments> variables() {
    return Stream.of(
        arguments(new byte[] {1,
                (byte) 0x9a, (byte) 0x88, 0x01, 0,
                2, 0, 0, 0,
                (byte) 0xf1, 0x05, 0, 0,

                (byte) 0xff, (byte) 0xff, (byte) 0xff, 0,
                5, 0, 0, 0,
                (byte) 0xd0, 0x07, 0, 0},

            new int[] {790, 52223, 8717572, 100506, 16777215})
    );
  }

  @ParameterizedTest
  @MethodSource("variables")
  void testApply(byte[] inputBytes, int[] outputInts) {
    Converter<BufferFrame, AperCalibrationCurrent1Variable> converter = LinkedConverter.of(
        new ToIntegerConverter<>(AperStage1Variable.class, 1000), AperCalibrationCurrent1Variable.class);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 3000 - 1; i++) {
      long count = converter.apply(bufferFrame).peek(ints -> {
        if (!processed.get()) {
          assertThat(ints).containsExactly(outputInts);
          processed.set(true);
        }
      }).count();
      if (processed.get()) {
        assertThat(count).isEqualTo(10);
        break;
      }
    }
    assertTrue(processed.get(), "Data are not converted!");
    assertThat(converter.getFrequency()).isEqualTo(1000.0);
  }

  @ParameterizedTest
  @EnumSource(value = AperCalibrationCurrent1Variable.class)
  void testVariableProperties(DependentVariable<AperStage1Variable, AperCalibrationCurrent1Variable> variable) {
    assertThat(variable.getUnit()).isEqualTo(AbstractUnit.ONE);
    assertThat(variable.getInputVariablesClass()).isEqualTo(AperStage1Variable.class);
  }

  @ParameterizedTest
  @EnumSource(value = AperCalibrationCurrent1Variable.class, mode = EnumSource.Mode.EXCLUDE, names = {"PU_1", "PU_2"})
  void testOptionsNotPU(Variable<AperCalibrationCurrent1Variable> variable) {
    assertThat(variable.options()).containsExactly(Variable.Option.TEXT_VALUE_BANNER);
  }

  @ParameterizedTest
  @EnumSource(value = AperCalibrationCurrent1Variable.class, mode = EnumSource.Mode.INCLUDE, names = {"PU_1", "PU_2"})
  void testOptionsPU(Variable<AperCalibrationCurrent1Variable> variable) {
    assertThat(variable.options()).containsExactly(Variable.Option.VISIBLE);
  }
}