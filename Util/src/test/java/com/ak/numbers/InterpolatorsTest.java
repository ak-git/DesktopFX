package com.ak.numbers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InterpolatorsTest {
  @Test
  void testTooLowCoefficients() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> Interpolators.interpolator(InterpolatorCoefficients.INTERPOLATOR_TEST_INVALID))
        .withMessageContaining("Number of points 1 from INTERPOLATOR_TEST_INVALID is too small");
  }

  static Stream<Arguments> interpolators() {
    return Stream.of(
        arguments(
            InterpolatorCoefficients.INTERPOLATOR_TEST_AKIMA, new int[] {
                0, 0, 0, 40, 100,
                133, 153, 163, 160, 146,
                120, 83, 33, -27, -100
            }
        ),
        arguments(
            InterpolatorCoefficients.INTERPOLATOR_TEST_LINEAR, new int[] {
                0, 0, 0, 8, 15, 23, 30, 38, 46, 53, 61, 69, 76, 84, 91
            }
        ),
        arguments(
            InterpolatorCoefficients.INTERPOLATOR_FILTER_TEST_LINEAR, IntStream.rangeClosed(1, 15).map(x -> x * 2).toArray()
        )
    );
  }

  @ParameterizedTest
  @MethodSource("interpolators")
  <C extends Enum<C> & Coefficients> void testInterpolator(C coefficients, int[] expected) {
    IntUnaryOperator operator = Interpolators.interpolator(coefficients).get();
    int[] actual = IntStream.rangeClosed(1, 15).map(operator).toArray();
    assertThat(actual).containsExactly(expected);
  }

  @ParameterizedTest
  @ValueSource(classes = FilterTestCoefficients.class)
  <C extends Enum<C> & Coefficients> void testBiInterpolator(Class<C> coeff) {
    IntBinaryOperator binaryOperator = Interpolators.interpolator(coeff).get();
    Assertions.assertAll(coeff.getName(),
        () -> assertThat(binaryOperator.applyAsInt(0, 0)).isZero(),
        () -> assertThat(binaryOperator.applyAsInt(0, 15)).isEqualTo(15),
        () -> assertThat(binaryOperator.applyAsInt(15, 0)).isZero(),
        () -> assertThat(binaryOperator.applyAsInt(15, 15)).isEqualTo(15),
        () -> assertThat(binaryOperator.applyAsInt(10, 10)).isZero(),
        () -> assertThat(binaryOperator.applyAsInt(11, 11)).isEqualTo(15),

        () -> assertThat(binaryOperator.applyAsInt(-1, -1)).isZero(),
        () -> assertThat(binaryOperator.applyAsInt(100, 100)).isEqualTo(15)
    );
  }
}