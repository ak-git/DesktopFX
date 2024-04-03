package com.ak.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.ak.util.Strings.OHM_METRE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class StringsTest {
  static Stream<Arguments> strings() {
    return Stream.of(
        arguments("CC1_1_2", "2"),
        arguments("CC_12", "12"),
        arguments("CC34", "34"),
        arguments("56", "56"),
        arguments("Pu", ""),
        arguments("1Pu", ""),
        arguments("P1u", ""),
        arguments("P_1_u", ""),
        arguments("", "")
    );
  }

  @ParameterizedTest
  @MethodSource("strings")
  void testNumberSuffix(String toExtract, String expected) {
    assertThat(Strings.numberSuffix(toExtract)).isEqualTo(expected);
  }

  @Test
  void testRhoPhi() {
    assertThat(Strings.rho(2.1234)).isEqualTo("ρ = %.3f %s", 2.123, OHM_METRE);
    assertThat(Strings.dRhoByPhi(1.21)).isEqualTo("dρ/dψ = %.3f %s", 1.21, OHM_METRE);
  }

  @Test
  void testRho() {
    assertThat(Strings.rho(1, 2.1234)).isEqualTo("ρ₁ = 2.1234 Ω·m");
  }

  @Test
  void testH() {
    assertThat(Strings.h(2, 2.1234)).isEqualTo("h₂ = 2.1234 mm");
  }
}