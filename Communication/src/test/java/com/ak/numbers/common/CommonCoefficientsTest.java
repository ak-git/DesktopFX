package com.ak.numbers.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class CommonCoefficientsTest {
  @Test
  void testCoefficients() {
    assertThat(CommonCoefficients.values()).hasSize(2);
  }

  @ParameterizedTest
  @EnumSource(CommonCoefficients.class)
  void testSize(SimpleCoefficients c) {
    assertThat(c.get()).hasSize(61);
  }
}