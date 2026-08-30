package com.ak.util;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import javax.measure.MetricPrefix;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.units.indriya.AbstractUnit.ONE;
import static tech.units.indriya.unit.Units.METRE;
import static tech.units.indriya.unit.Units.PERCENT;

class MetricsTest {
  @Test
  void milli() {
    assertThat(Metrics.Length.MILLI.toSI(1.0)).isCloseTo(0.001, Offset.offset(1.0e-3));
    assertThat(Metrics.Length.MILLI.toSI(-2.0)).isCloseTo(-0.002, Offset.offset(1.0e-3));
    assertThat(Metrics.Length.MILLI.toSI(Double.NaN)).isNaN();
    assertThat(Metrics.Length.MILLI.toSI(Double.POSITIVE_INFINITY)).isInfinite();
    assertThat(Metrics.Length.MILLI.toSI(Double.NEGATIVE_INFINITY)).isInfinite();
  }

  @Test
  void fromMilli() {
    assertThat(Metrics.Length.MILLI.to(1.0, METRE)).isCloseTo(0.001, Offset.offset(1.0e-3));
    assertThat(Metrics.Length.MILLI.to(-2.1, METRE)).isCloseTo(-0.0021, Offset.offset(1.0e-4));
  }

  @Test
  void milliToSI() {
    assertThat(Metrics.Length.MILLI.toSI(1.0)).isCloseTo(0.001, Offset.offset(1.0e-3));
    assertThat(Metrics.Length.MILLI.toSI(-2.1)).isCloseTo(-0.0021, Offset.offset(1.0e-4));
  }

  @Test
  void toMilli() {
    assertThat(Metrics.Length.METRE.to(1.0, MetricPrefix.MILLI(METRE))).isEqualTo(1000.0);
    assertThat(Metrics.Length.METRE.to(-2.1, MetricPrefix.MILLI(METRE))).isEqualTo(-2100.0);
  }

  @Test
  void fromPercents() {
    assertThat(Metrics.Dimensionless.PERCENT.to(100.0, ONE)).isOne();
    assertThat(Metrics.Dimensionless.PERCENT.to(-3.2, ONE)).isEqualTo(-0.032);
  }

  @Test
  void toSI() {
    assertThat(Metrics.Dimensionless.PERCENT.toSI(100.0)).isOne();
    assertThat(Metrics.Dimensionless.PERCENT.toSI(-3.2)).isEqualTo(-0.032);
  }

  @Test
  void toPercents() {
    assertThat(Metrics.Dimensionless.ONE.to(1.0, PERCENT)).isEqualTo(100.0);
    assertThat(Metrics.Dimensionless.ONE.to(-0.032, PERCENT)).isEqualTo(-3.2);
  }
}