package com.ak.comm.converter.briko;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.unit.Units;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ak.comm.converter.Variable.Option.TEXT_VALUE_BANNER;
import static com.ak.comm.converter.Variable.Option.VISIBLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.PASCAL;

class BrikoStage4VariableTest {
  static Stream<Arguments> variables() {
    return Stream.of(
        arguments(
            new byte[] {
                0x74, 0x20,
                (byte) 0xC1, 0x02, (byte) 0x8A, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xC2, (byte) 0xCB, 0x15, 0x00, 0x00,
                (byte) 0xC3, 0x1F, 0x0F, 0x00, 0x00,
                (byte) 0xC4, (byte) 0xF1, 0x07, 0x00, 0x00,
                (byte) 0xC5, 0x57, 0x26, 0x00, 0x00,
                (byte) 0xC6, (byte) 0xD8, 0x03, 0x00, 0x00,
            },

            new int[] {0, 0, 0, 0})
    );
  }

  @ParameterizedTest
  @MethodSource("variables")
  @ParametersAreNonnullByDefault
  void testApply(byte[] inputBytes, int[] outputInts) {
    Converter<BufferFrame, BrikoStage4Variable> converter = LinkedConverter.of(new BrikoConverter(), BrikoStage2Variable.class)
        .chainInstance(BrikoStage3Variable.class).chainInstance(BrikoStage4Variable.class);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 70 - 1; i++) {
      long count = converter.apply(bufferFrame).peek(ints -> {
        if (!processed.get()) {
          assertThat(ints).containsExactly(outputInts);
          processed.set(true);
        }
      }).count();
      if (processed.get()) {
        assertThat(count).isEqualTo(4L);
        break;
      }
    }
    assertAll(converter.toString(),
        () -> assertTrue(processed.get(), "Data are not converted!"),
        () -> assertThat(converter.getFrequency()).isEqualTo(380.0)
    );
  }

  @Test
  void testOptions() {
    assertThat(EnumSet.allOf(BrikoStage4Variable.class).stream().flatMap(v -> v.options().stream()).collect(Collectors.toSet()))
        .contains(VISIBLE, TEXT_VALUE_BANNER);
  }

  @ParameterizedTest
  @EnumSource(value = BrikoStage4Variable.class)
  void testFilterDelay(@Nonnull Variable<BrikoStage4Variable> variable) {
    assertThat(variable.filter().getDelay()).isZero();
  }

  @Test
  void testGetInputVariables() {
    assertThat(EnumSet.allOf(BrikoStage3Variable.class).stream().mapToInt(value -> value.getInputVariables().size()).toArray())
        .containsExactly(2, 2, 1);
  }

  @Test
  void testGetUnit() {
    assertThat(EnumSet.allOf(BrikoStage4Variable.class).stream().map(Variable::getUnit))
        .isEqualTo(List.of(Units.GRAM, Units.GRAM, MILLI(METRE), PASCAL));
  }

  @ParameterizedTest
  @EnumSource(value = BrikoStage4Variable.class)
  void testInputVariablesClass(@Nonnull DependentVariable<BrikoStage3Variable, BrikoStage4Variable> variable) {
    assertThat(variable.getInputVariablesClass()).isEqualTo(BrikoStage3Variable.class);
  }
}