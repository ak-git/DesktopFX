package com.ak.comm.converter.aper;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.unit.MetricPrefix;

import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tec.uom.se.unit.Units.*;

class AperStage6Current1Variable10mmTest {
  static Stream<Arguments> variables() {
    return Stream.of(
        arguments(new byte[] {1,
                (byte) 0x9a, (byte) 0x88, 0x01, 0,
                2, 0, 0, 0,
                (byte) 0xf1, 0x05, 0, 0,

                (byte) 0xff, (byte) 0xff, (byte) 0xff, 0,
                5, 0, 0, 0,
                (byte) 0xd0, 0x07, 0, 0},

            new int[] {55318, 5982, 330990, 5982, 3476, 13864, 1302}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("variables")
  void testApply(byte[] inputBytes, int[] outputInts) {
    Converter<BufferFrame, AperStage6Current1Variable10mm> converter = LinkedConverter
        .of(new ToIntegerConverter<>(AperStage1Variable.class, 1000), AperStage2UnitsVariable.class)
        .chainInstance(AperStage3Variable.class)
        .chainInstance(AperStage4Current1Variable.class)
        .chainInstance(AperStage5Current1Variable.class)
        .chainInstance(AperStage6Current1Variable10mm.class);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 500 - 1; i++) {
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
  @EnumSource(value = AperStage6Current1Variable10mm.class)
  void testGetInputVariables(DependentVariable<AperStage5Current1Variable, AperStage6Current1Variable10mm> variable) {
    assertThat(variable.getInputVariables()).hasSize(1);
  }

  @Test
  void testGetUnit() {
    assertThat(EnumSet.allOf(AperStage6Current1Variable10mm.class).stream().map(DependentVariable::getUnit))
        .isEqualTo(
            List.of(
                MetricPrefix.MILLI(OHM), MetricPrefix.MICRO(VOLT),
                MetricPrefix.MILLI(OHM), MetricPrefix.MICRO(VOLT),
                MetricPrefix.MILLI(OHM).multiply(METRE), MetricPrefix.MILLI(OHM).multiply(METRE),
                OHM)
        );
  }

  @Test
  void testOptions() {
    assertThat(EnumSet.allOf(AperStage6Current1Variable10mm.class).stream().flatMap(v -> v.options().stream()))
        .isEqualTo(
            List.of(
                Variable.Option.VISIBLE, Variable.Option.VISIBLE,
                Variable.Option.TEXT_VALUE_BANNER, Variable.Option.TEXT_VALUE_BANNER, Variable.Option.TEXT_VALUE_BANNER
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = AperStage6Current1Variable10mm.class)
  void testFilterDelay(Variable<AperStage6Current1Variable10mm> variable) {
    assertThat(variable.filter().getDelay()).isZero();
  }

  @ParameterizedTest
  @EnumSource(value = AperStage6Current1Variable10mm.class)
  void testInputVariablesClass(DependentVariable<AperStage5Current1Variable, AperStage6Current1Variable10mm> variable) {
    assertThat(variable.getInputVariablesClass()).isEqualTo(AperStage5Current1Variable.class);
  }
}