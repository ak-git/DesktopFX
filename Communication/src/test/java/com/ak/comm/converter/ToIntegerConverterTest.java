package com.ak.comm.converter;

import com.ak.comm.bytes.BufferFrame;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.AbstractUnit;

import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ToIntegerConverterTest {
  static Stream<Arguments> variables() {
    return Stream.of(
        arguments(
            ADCVariable.class, new byte[] {1, 2, 3, 4, 5, 6},
            new int[] {2 + (3 << 8) + (4 << 16) + (5 << 24)}
        ),
        arguments(
            TwoVariables.class, new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9},
            new int[] {2 + (3 << 8) + (4 << 16) + (5 << 24), 6 + (7 << 8) + (8 << 16) + (9 << 24)}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("variables")
  <T extends Enum<T> & Variable<T>> void testApply(Class<T> evClass, byte[] inputBytes, int[] outputInts) {
    ToIntegerConverter<T> converter = new ToIntegerConverter<>(evClass, 1000);
    assertThat(converter.variables()).containsSequence(EnumSet.allOf(evClass));
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