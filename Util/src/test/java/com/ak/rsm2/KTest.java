package com.ak.rsm2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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
  void value(double rho1, double rho2, double expected) {
    K k = K.of(rho1, rho2);
    Assertions.assertAll(k.toString(),
        () -> assertThat(k.value()).isCloseTo(expected, within(0.001)),
        () -> assertThat(k).hasToString("%.3f".formatted(expected))
    );
  }

  @ParameterizedTest
  @CsvSource(delimiter = '|', textBlock = """
       1.0 |  1.0
       1.0 | 10.0
      10.0 |  1.0
      """)
  void rho2(double rho1, double rho2) {
    K k = K.of(rho1, rho2);
    Assertions.assertAll(k.toString(),
        () -> assertThat(k.getRho2(rho1)).isCloseTo(rho2, within(0.001))
    );
  }

  @Test
  void plusOne() {
    K one = K.of(1.0);
    Assertions.assertAll("k = +1",
        () -> assertThat(K.of(0.0, 1.0).isPlusOne()).isTrue(),
        () -> assertThat(K.of(1.0, Double.POSITIVE_INFINITY).isPlusOne()).isTrue(),
        () -> assertThat(one.isPlusOne()).isTrue(),
        () -> assertThat(one.isMinusOne()).isFalse(),
        () -> assertThat(one.getRho2(1.0)).isInfinite()
    );
  }

  @Test
  void zero() {
    K zero = K.of(0.0);
    Assertions.assertAll("k = 0",
        () -> assertThat(zero.isPlusOne()).isFalse(),
        () -> assertThat(zero.isMinusOne()).isFalse(),
        () -> assertThat(zero.getRho2(1.0)).isOne()
    );

  }

  @Test
  void minusOne() {
    K minus = K.of(-1.0);
    Assertions.assertAll("k = -1",
        () -> assertThat(K.of(1.0, 0.0).isMinusOne()).isTrue(),
        () -> assertThat(K.of(Double.POSITIVE_INFINITY, 1.0).isMinusOne()).isTrue(),
        () -> assertThat(minus.isPlusOne()).isFalse(),
        () -> assertThat(minus.isMinusOne()).isTrue(),
        () -> assertThat(minus.getRho2(1.0)).isZero()
    );
  }
}