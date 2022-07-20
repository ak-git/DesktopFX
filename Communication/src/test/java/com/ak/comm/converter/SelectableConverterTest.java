package com.ak.comm.converter;

import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class SelectableConverterTest {
  static Stream<Arguments> variables() {
    return Stream.of(Arguments.arguments(new int[] {6, 2}, new int[] {6 + 2, 6 - 2, 6 / 2}));
  }

  @ParameterizedTest
  @MethodSource("variables")
  @ParametersAreNonnullByDefault
  void testApply(int[] input, int[] output) {
    Function<Stream<int[]>, Stream<int[]>> converter = new SelectableConverter<>(OperatorVariables.class, 1000);
    assertThat(converter.apply(Stream.of(input))).containsExactly(output).hasSize(1);
  }
}