package com.ak.util;

import javax.annotation.Nonnegative;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class NumbersTest {
  @ParameterizedTest
  @ValueSource(ints = {2, 10, 100})
  void rangeLog(@Nonnegative int size) {
    assertThat(Numbers.rangeLog(0.01, 100, size)).hasSize(size);
  }

  @Test
  void round() {
    assertThat(Numbers.round(0.1).applyAsDouble(0.01)).isCloseTo(0.0, Assertions.byLessThan(0.01));
    assertThat(Numbers.round(0.1).applyAsDouble(0.1)).isCloseTo(0.1, Assertions.byLessThan(0.01));
    assertThat(Numbers.round(0.1).applyAsDouble(1.0)).isCloseTo(1.0, Assertions.byLessThan(0.01));
  }
}