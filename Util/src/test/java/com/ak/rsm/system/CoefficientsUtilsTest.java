package com.ak.rsm.system;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class CoefficientsUtilsTest {
  @Test
  void testSerialize() {
    double[] out = CoefficientsUtils.serialize(new double[] {1.0, -1.0, 3.0, -3.0}, new double[] {1.0, -1.0}, 5);
    assertThat(out).containsExactly(new double[] {1.0, 0.0, 3.0, 0.0, 0.0}, byLessThan(1.0e-3));
  }
}