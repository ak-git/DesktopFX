package com.ak.numbers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.IntSummaryStatistics;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RangeUtilsTest {
  static Stream<Arguments> coefficients() {
    return Stream.of(
        arguments(RangeUtils.rangeX(InterpolatorCoefficients.class), 1, 16),
        arguments(RangeUtils.rangeY(InterpolatorCoefficients.class), -100, 100)
    );
  }

  @ParameterizedTest
  @MethodSource("coefficients")
  void testCoefficients(IntSummaryStatistics statistics, int min, int max) {
    assertThat(statistics.getMin()).isEqualTo(min);
    assertThat(statistics.getMax()).isEqualTo(max);
  }

  @Test
  void testReverseOrder() {
    assertThat(RangeUtils.reverseOrder(new double[] {1.0, 2.0, -10.0, 3.0})).containsExactly(3.0, -10.0, 2.0, 1.0);
  }
}