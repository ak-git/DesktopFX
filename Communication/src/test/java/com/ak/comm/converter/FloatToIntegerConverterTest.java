package com.ak.comm.converter;

import com.ak.comm.bytes.BufferFrame;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.AbstractUnit;

import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FloatToIntegerConverterTest {
  static Stream<Arguments> variables() {
    return Stream.of(
        arguments(
            ADCVariable.class, new byte[] {3, 0, 0, (byte) 0x80, 0x3f},
            new int[] {1}
        ),
        arguments(
            TwoVariables.class, new byte[] {3, 0, 0, (byte) 0x80, 0x3f, 0, 0, (byte) 0x80, 0x3f},
            new int[] {1, 1}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("variables")
  <T extends Enum<T> & Variable<T>> void testApply(Class<T> evClass, byte[] inputBytes, int[] outputInts) {
    FloatToIntegerConverter<T> converter = new FloatToIntegerConverter<>(evClass, 1000);
    assertThat(EnumSet.allOf(evClass)).containsSequence(converter.variables());
    assertThat(EnumSet.allOf(evClass)).isNotEmpty().allSatisfy(t -> assertThat(t.getUnit()).isEqualTo(AbstractUnit.ONE));
    AtomicBoolean processed = new AtomicBoolean();
    converter.apply(new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN)).
        forEach(ints -> {
          assertThat(ints).containsExactly(outputInts);
          processed.set(true);
        });
    assertTrue(processed.get(), "Data are not converted!");
  }
}