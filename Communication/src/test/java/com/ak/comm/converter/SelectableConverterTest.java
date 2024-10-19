package com.ak.comm.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SelectableConverterTest {
  static Stream<Arguments> variables() {
    return Stream.of(Arguments.arguments(new int[] {6, 2}, new int[] {6 + 2, 6 - 2, 6 / 2}));
  }

  @ParameterizedTest
  @MethodSource("variables")
  void testApply(int[] input, int[] output) {
    Function<Stream<int[]>, Stream<int[]>> converter = new SelectableConverter<>(OperatorVariables.class, 1000);
    assertThat(converter.apply(Stream.of(input))).containsExactly(output).hasSize(1);
  }
}