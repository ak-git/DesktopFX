package com.ak.comm.converter.aper;

import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.Variable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.AbstractUnit;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AperStage1VariableTest {
  static Stream<Arguments> variables() {
    return Stream.of(
        arguments(new byte[] {1,
                (byte) 0x9a, (byte) 0x88, 0x01, 0,
                2, 0, 0, 0,
                (byte) 0xf1, 0x05, 0, 0,

                (byte) 0xff, (byte) 0xff, (byte) 0xff, 0,
                5, 0, 0, 0,
                (byte) 0xd0, 0x07, 0, 0},

            new int[] {100506, -728504, 1521, 16777215, -728503, 2000})
    );
  }

  @ParameterizedTest
  @MethodSource("variables")
  @ParametersAreNonnullByDefault
  void testApply(byte[] inputBytes, int[] outputInts) {
    Converter<BufferFrame, AperStage1Variable> converter = new ToIntegerConverter<>(AperStage1Variable.class, 1000);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    converter.apply(bufferFrame).forEach(ints -> {
      assertThat(ints).containsExactly(outputInts);
      processed.set(true);
    });
    assertTrue(processed.get(), "Data are not converted!");
    assertThat(converter.getFrequency()).isEqualTo(1000.0);
  }

  @ParameterizedTest
  @EnumSource(value = AperStage1Variable.class, names = {"R1", "R2", "CCU1", "CCU2"})
  void testUnitR(@Nonnull Variable<AperStage1Variable> variable) {
    assertThat(variable.getUnit()).isEqualTo(AbstractUnit.ONE);
  }

  @ParameterizedTest
  @EnumSource(value = AperStage1Variable.class, names = {"E1", "E2"})
  void testUnitE(@Nonnull Variable<AperStage1Variable> variable) {
    assertThat(variable.getUnit()).isEqualTo(MetricPrefix.MICRO(Units.VOLT));
  }
}