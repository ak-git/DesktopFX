package com.ak.rsm2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class SignTest {
  @Test
  void values() {
    assertThat(EnumSet.allOf(Sign.class)).hasSize(2);
  }

  @ParameterizedTest
  @EnumSource(value = Sign.class, mode = EnumSource.Mode.INCLUDE)
  void applyAsDouble(Sign sign) {
    double d = Math.random() + 1.0;
    double expected = switch (sign) {
      case PLUS -> d;
      case MINUS -> -d;
    };
    assertThat(sign.applyAsDouble(d)).isEqualTo(expected);
  }
}