package com.ak.numbers;

import com.ak.util.Extension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CoefficientsTest {
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


  static Stream<Arguments> countCoefficients() {
    return Stream.of(
        arguments(InterpolatorCoefficients.INTERPOLATOR_TEST_AKIMA, 10),
        arguments(InterpolatorCoefficients.INTERPOLATOR_TEST_LINEAR, 8),
        arguments(InterpolatorCoefficients.INTERPOLATOR_TEST_INVALID, 2)
    );
  }

  @ParameterizedTest
  @MethodSource("countCoefficients")
  void testCoefficients(Supplier<double[]> coefficients, @Nonnegative int count) {
    assertThat(coefficients.get()).hasSize(count);
  }

  @Test
  void testRead() throws IOException {
    try (InputStream resourceAsStream = getClass().getResourceAsStream(Extension.TXT.attachTo("DIFF"))) {
      Scanner scanner = new Scanner(Objects.requireNonNull(resourceAsStream), Charset.defaultCharset());
      assertThat(Coefficients.read(scanner)).containsExactly(-1.0, 0.0, 1.0);
    }
  }
}