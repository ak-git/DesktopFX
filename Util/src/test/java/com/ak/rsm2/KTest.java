package com.ak.rsm2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class KTest {
  static Stream<Arguments> k() {
    return Stream.of(
        arguments(1.0, 1.0, 0.0),
        arguments(1.0, Double.POSITIVE_INFINITY, 1.0),
        arguments(1.0, 0.0, -1.0),
        arguments(Double.POSITIVE_INFINITY, 1.0, -1.0),
        arguments(10, 1.0, -9.0 / 11.0),
        arguments(1.0, 10.0, 9.0 / 11.0)
    );
  }

  @ParameterizedTest
  @MethodSource("k")
  void value(double rho1, double rho2, double k) {
    assertThat(K.of(rho1, rho2).value()).isCloseTo(k, within(0.001));
  }

  @Test
  void plusOne() {
    Assertions.assertAll("k = +1",
        () -> assertThat(K.of(0.0, 1.0).isPlusOne()).isTrue(),
        () -> assertThat(K.of(1.0, Double.POSITIVE_INFINITY).isPlusOne()).isTrue(),
        () -> assertThat(K.of(1.0).isPlusOne()).isTrue(),
        () -> assertThat(K.of(1.0).isZero()).isFalse(),
        () -> assertThat(K.of(1.0).isMinusOne()).isFalse()
    );
  }

  @Test
  void zero() {
    Assertions.assertAll("k = 0",
        () -> assertThat(K.of(0.0, 0.0).isZero()).isTrue(),
        () -> assertThat(K.of(1.0, 1.0).isZero()).isTrue(),
        () -> assertThat(K.of(0.0).isPlusOne()).isFalse(),
        () -> assertThat(K.of(0.0).isZero()).isTrue(),
        () -> assertThat(K.of(0.0).isMinusOne()).isFalse()
    );

  }

  @Test
  void minusOne() {
    Assertions.assertAll("k = -1",
        () -> assertThat(K.of(1.0, 0.0).isMinusOne()).isTrue(),
        () -> assertThat(K.of(Double.POSITIVE_INFINITY, 1.0).isMinusOne()).isTrue(),
        () -> assertThat(K.of(-1.0).isPlusOne()).isFalse(),
        () -> assertThat(K.of(-1.0).isZero()).isFalse(),
        () -> assertThat(K.of(-1.0).isMinusOne()).isTrue()
    );
  }
}