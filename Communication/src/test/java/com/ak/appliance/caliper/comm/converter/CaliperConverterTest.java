package com.ak.appliance.caliper.comm.converter;

import com.ak.comm.converter.Variable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CaliperConverterTest {
  static Stream<Arguments> variables() {
    return Stream.of(
        arguments(
            new byte[] {51, 102, 102, 53},
            new int[] {4}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("variables")
  <T extends Enum<T> & Variable<T>> void testApply(byte[] inputBytes, int[] outputInts) {
    Function<String, Stream<int[]>> converter = new CaliperConverter();
    AtomicBoolean processed = new AtomicBoolean();
    converter.apply(new String(inputBytes)).
        forEach(ints -> {
          assertThat(ints).containsExactly(outputInts);
          processed.set(true);
        });
    assertTrue(processed.get(), "Data are not converted!");
  }
}