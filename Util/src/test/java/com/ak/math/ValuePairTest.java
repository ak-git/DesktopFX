package com.ak.math;

import com.ak.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.security.SecureRandom;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

import static com.ak.util.Strings.OHM_METRE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ValuePairTest {
  private static final RandomGenerator RND = new SecureRandom();

  @Test
  void testGetValue() {
    double value = RND.nextDouble();
    double absError = RND.nextDouble();
    ValuePair valuePair = ValuePair.Name.NONE.of(value, absError);
    assertThat(valuePair.value()).isEqualTo(value);
    assertThat(valuePair.absError()).isEqualTo(absError);
  }

  static Stream<Arguments> toStrings() {
    return Stream.of(
        arguments(ValuePair.Name.NONE.of(1.2345, 0.19), "%.1f ± %.2f".formatted(1.2345, 0.19)),
        arguments(ValuePair.Name.RHO.of(1.2345, 0.19), "ρ = %.1f ± %.2f %s".formatted(1.2345, 0.19, OHM_METRE)),
        arguments(ValuePair.Name.RHO_1.of(1.2345, 0.19), "ρ₁ = %.1f ± %.2f %s".formatted(1.2345, 0.19, OHM_METRE)),
        arguments(ValuePair.Name.RHO_2.of(1.5345, 0.19), "ρ₂ = %.1f ± %.2f %s".formatted(1.5345, 0.19, OHM_METRE)),
        arguments(ValuePair.Name.RHO_3.of(1.6345, 0.19), "ρ₃ = %.1f ± %.2f %s".formatted(1.6345, 0.19, OHM_METRE)),
        arguments(ValuePair.Name.H.of(1.2345, 0.011), "h = %.0f ± %.1f mm".formatted(1234.5, 11.0)),
        arguments(ValuePair.Name.K12.of(1.2345, 0.0), "k₁₂ = %.6f".formatted(1.2345)),
        arguments(ValuePair.Name.K23.of(1.2345, 0.0), "k₂₃ = %.6f".formatted(1.2345)),
        arguments(ValuePair.Name.H_L.of(Double.NaN, 0.0), "%s = %f".formatted(Strings.PHI, Double.NaN))
    );
  }

  @ParameterizedTest
  @MethodSource("toStrings")
  @ParametersAreNonnullByDefault
  void testTestToString(ValuePair valuePair, String toString) {
    assertThat(valuePair).hasToString(toString);
  }

  static Stream<Arguments> checkEquals() {
    ValuePair actual = ValuePair.Name.NONE.of(1.23451, 0.19);
    return Stream.of(
        arguments(actual, actual),
        arguments(ValuePair.Name.NONE.of(1.23451, 0.19), ValuePair.Name.NONE.of(1.23452, 0.19))
    );
  }

  @ParameterizedTest
  @MethodSource("checkEquals")
  @ParametersAreNonnullByDefault
  void testEquals(ValuePair v1, ValuePair v2) {
    assertThat(v1).isEqualTo(v2).hasSameHashCodeAs(v2);
    assertThat(v2).isEqualTo(v1).hasSameHashCodeAs(v1);
  }

  static Stream<Arguments> checkNotEquals() {
    return Stream.of(
        arguments(new Object(), ValuePair.Name.NONE.of(1.23451, 0.19)),
        arguments(ValuePair.Name.NONE.of(1.23451, 0.19), new Object()),
        arguments(ValuePair.Name.NONE.of(1.3, 0.19), ValuePair.Name.NONE.of(1.23452, 0.19))
    );
  }

  @ParameterizedTest
  @MethodSource("checkNotEquals")
  @ParametersAreNonnullByDefault
  void testNotEquals(Object v1, Object v2) {
    assertThat(v1).isNotEqualTo(v2).doesNotHaveSameHashCodeAs(v2);
    assertThat(v2).isNotEqualTo(v1).doesNotHaveSameHashCodeAs(v1);
  }

  @Test
  void testMerge() {
    ValuePair v1 = ValuePair.Name.NONE.of(10.0, 1.0);
    ValuePair v2 = ValuePair.Name.NONE.of(30.0, 1.0);
    ValuePair v3 = ValuePair.Name.NONE.of(50.0, 1.0);
    ValuePair v4 = ValuePair.Name.NONE.of(30.0, 1.0);

    ValuePair merged = v1.mergeWith(v2).mergeWith(v3).mergeWith(v4);
    assertAll(merged.toString(),
        () -> assertThat(merged.value()).isCloseTo(30.0, byLessThan(0.1)),
        () -> assertThat(merged.absError()).isCloseTo(0.5, byLessThan(0.1))
    );
  }
}