package com.ak.util;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsTest {
  @Test
  void testFromMilli() {
    assertThat(Metrics.fromMilli(1.0)).isEqualTo(0.001, Offset.offset(1.0e-3));
    assertThat(Metrics.fromMilli(-2.1)).isEqualTo(-0.0021, Offset.offset(1.0e-4));
  }

  @Test
  void testToMilli() {
    assertThat(Metrics.toMilli(1.0)).isEqualTo(1000.0);
    assertThat(Metrics.toMilli(-2.1)).isEqualTo(-2100.0);
  }

  @Test
  void testFromPercents() {
    assertThat(Metrics.fromPercents(100.0)).isEqualTo(1.0);
    assertThat(Metrics.fromPercents(-3.2)).isEqualTo(-0.032);
  }

  @Test
  void testToPercents() {
    assertThat(Metrics.toPercents(1.0)).isEqualTo(100.0);
    assertThat(Metrics.toPercents(-0.032)).isEqualTo(-3.2);
  }
}