package com.ak.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.annotation.Nonnegative;

import static org.assertj.core.api.Assertions.assertThat;

class NumbersTest {
  @ParameterizedTest
  @ValueSource(ints = {2, 10, 100})
  void rangeLog(@Nonnegative int size) {
    assertThat(Numbers.rangeLog(0.01, 100, size)).hasSize(size);
  }

  @ParameterizedTest
  @CsvSource({"0.1,0.01,0.0", "0.1,0.1,0.1", "0.1,1.0,1.0"})
  void round(@Nonnegative double step, @Nonnegative double operand, @Nonnegative double expected) {
    assertThat(Numbers.round(step).applyAsDouble(operand)).isCloseTo(expected, Assertions.byLessThan(0.01));
  }

  @ParameterizedTest
  @CsvSource({"-0.6,-1", "-0.5,0", "-0.1,0", "0.0,0", "0.1,0", "0.5,1"})
  void toInt(double d, int expected) {
    assertThat(Numbers.toInt(d)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({"3,0", "4,1"})
  void log10ToInt(double d, int expected) {
    assertThat(Numbers.log10ToInt(d)).isEqualTo(expected);
  }
}