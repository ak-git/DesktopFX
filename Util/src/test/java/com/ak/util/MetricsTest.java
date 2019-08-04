package com.ak.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MetricsTest {
  @Test
  public void testFromMilli() {
    Assert.assertEquals(Metrics.fromMilli(1.0), 0.001, 1.0e-3);
    Assert.assertEquals(Metrics.fromMilli(-2.1), -0.0021, 1.0e-4);
  }

  @Test
  public void testToMilli() {
    Assert.assertEquals(Metrics.toMilli(1.0), 1000.0, 1.0e-3);
    Assert.assertEquals(Metrics.toMilli(-2.1), -2100.0, 1.0e-4);
  }

  @Test
  public void testFromPercents() {
    Assert.assertEquals(Metrics.fromPercents(100.0), 1.0, 1.0e-3);
    Assert.assertEquals(Metrics.fromPercents(-3.2), -0.032, 1.0e-3);
  }
}