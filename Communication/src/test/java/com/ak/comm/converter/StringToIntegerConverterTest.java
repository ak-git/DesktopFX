package com.ak.comm.converter;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.AbstractUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class StringToIntegerConverterTest {
  static Stream<Arguments> variables() {
    return Stream.of(
        arguments(
            ADCVariable.class, new byte[] {51, 102, 102, 53},
            new int[] {16373}
        ),
        arguments(
            TwoVariables.class, new byte[] {51, 102, 102, 53},
            new int[] {16373, 16373}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("variables")
  @ParametersAreNonnullByDefault
  <T extends Enum<T> & Variable<T>> void testApply(Class<T> evClass, byte[] inputBytes, int[] outputInts) {
    StringToIntegerConverter<T> converter = new StringToIntegerConverter<>(evClass, 1000);
    assertThat(EnumSet.allOf(evClass)).containsSequence(converter.variables());
    assertThat(EnumSet.allOf(evClass)).isNotEmpty().allSatisfy(t -> assertThat(t.getUnit()).isEqualTo(AbstractUnit.ONE));
    AtomicBoolean processed = new AtomicBoolean();
    converter.apply(new String(inputBytes)).
        forEach(ints -> {
          assertThat(ints).containsExactly(outputInts);
          processed.set(true);
        });
    assertTrue(processed.get(), "Data are not converted!");
  }
}