package com.ak.inverse;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class InequalityTest {
  @Test
  void testProportional() {
    Inequality inequality = Inequality.proportional();
    assertThat(inequality.applyAsDouble(-12, -3)).isCloseTo(3.0, byLessThan(0.01));
    assertThat(inequality.getAsDouble()).isCloseTo(3.0, byLessThan(0.01));
    assertThat(inequality.applyAsDouble(12, -4)).isCloseTo(5.0, byLessThan(0.01));
    assertThat(inequality.applyAsDouble(new double[] {8.0 + 1.0, 8.0 + 1.0, 4.0 + 1.0}, new double[] {1.0, 1.0, 1.0}))
        .isCloseTo(13.0, byLessThan(0.01));
    assertThat(inequality.applyAsDouble(0.0, 0.0)).isNaN();
    assertThat(inequality.applyAsDouble(1.0, 0.0)).isInfinite();
  }

  @Test
  void testL2Absolute() {
    Inequality inequality = Inequality.absolute();
    assertThat(inequality.applyAsDouble(0.0, -3.0)).isCloseTo(3.0, byLessThan(0.01));
    assertThat(inequality.getAsDouble()).isCloseTo(3.0, byLessThan(0.01));
    assertThat(inequality.applyAsDouble(4.0, 0.0)).isCloseTo(5.0, byLessThan(0.01));
    assertThat(inequality.applyAsDouble(new double[] {-1.0, 2.0}, new double[] {-1.0, 2.0})).isCloseTo(5.0, byLessThan(0.01));
    assertThat(inequality.applyAsDouble(new double[] {8.0, 0.0, 0.0}, new double[] {0.0, 8.0, 4.0})).isCloseTo(13.0, byLessThan(0.01));
  }
}