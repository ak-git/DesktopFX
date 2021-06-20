package com.ak.math;

import java.security.SecureRandom;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ValuePairTest {
  private static final Random RND = new SecureRandom();

  @Test
  public void testGetValue() {
    double expected = RND.nextDouble();
    Assert.assertEquals(new ValuePair(expected).getValue(), expected, 1.0e-6);
    Assert.assertEquals(new ValuePair(expected).getAbsError(), 0.0, 1.0e-6);
  }

  @Test
  public void testGetAbsError() {
    double expected = Math.abs(RND.nextDouble());
    Assert.assertEquals(new ValuePair(RND.nextDouble(), expected).getAbsError(), expected, 1.0e-6);
  }

  @Test
  public void testTestToString() {
    Assert.assertEquals(new ValuePair(1.2345, 0.19).toString(), "%.1f ± %.2f".formatted(1.2345, 0.19));
    Assert.assertEquals(new ValuePair(1.2345, 0.011).toString(), "%.2f ± %.3f".formatted(1.2345, 0.011));
    Assert.assertEquals(new ValuePair(1.2345).toString(), Double.toString(1.2345));
    Assert.assertEquals(new ValuePair(Double.NaN).toString(), Double.toString(Double.NaN));
  }

  @Test
  public void testEquals() {
    ValuePair actual = new ValuePair(1.23451, 0.19);
    Assert.assertEquals(actual, actual);
    Assert.assertNotEquals(new Object(), actual);
    Assert.assertEquals(new ValuePair(1.23451, 0.19), new ValuePair(1.23452, 0.19));
    Assert.assertNotEquals(new ValuePair(1.3, 0.19), new ValuePair(1.23452, 0.19));
    Assert.assertEquals(new ValuePair(1.23451, 0.19).hashCode(), new ValuePair(1.23452, 0.19).hashCode());
    Assert.assertNotEquals(new ValuePair(1.3, 0.1).hashCode(), new ValuePair(1.23452, 0.1).hashCode());
  }

  @Test
  public void testMerge() {
    ValuePair v1 = new ValuePair(10.0, 1.0);
    ValuePair v2 = new ValuePair(30.0, 2.0);
    ValuePair v3 = new ValuePair(50.0, 1.0);

    ValuePair merged = v1.mergeWith(v2).mergeWith(v3);
    Assert.assertEquals(merged.getValue(), 30.0, 0.1, merged.toString());
    Assert.assertEquals(merged.getAbsError(), 0.33, 0.1, merged.toString());
  }
}