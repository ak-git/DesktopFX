package com.ak.math;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ValuePairTest {
  private static final RandomGenerator RND = new SecureRandom();

  @Test
  public void testGetValue() {
    double value = RND.nextDouble();
    double absError = RND.nextDouble();
    ValuePair valuePair = ValuePair.Name.NONE.of(value, absError);
    Assert.assertEquals(valuePair.getValue(), value, 1.0e-6);
    Assert.assertEquals(valuePair.getAbsError(), absError, 1.0e-6);
  }

  @Test
  public void testTestToString() {
    Assert.assertEquals(ValuePair.Name.NONE.of(1.2345, 0.19).toString(), "%.1f ± %.2f".formatted(1.2345, 0.19));
    Assert.assertEquals(ValuePair.Name.H.of(1.2345, 0.011).toString(), "h = %.0f ± %.1f mm".formatted(1234.5, 11.0));
    Assert.assertEquals(ValuePair.Name.K12.of(1.2345, 0.0).toString(), "k₁₂ = %.6f".formatted(1.2345));
    Assert.assertEquals(ValuePair.Name.H_L.of(Double.NaN, 0.0).toString(), "%s = %f".formatted(Strings.PHI, Double.NaN));
  }

  @Test
  public void testEquals() {
    ValuePair actual = ValuePair.Name.NONE.of(1.23451, 0.19);
    Assert.assertEquals(actual, actual);
    Assert.assertNotEquals(new Object(), actual);
    Assert.assertEquals(ValuePair.Name.NONE.of(1.23451, 0.19), ValuePair.Name.NONE.of(1.23452, 0.19));
    Assert.assertNotEquals(ValuePair.Name.NONE.of(1.3, 0.19), ValuePair.Name.NONE.of(1.23452, 0.19));
    Assert.assertEquals(ValuePair.Name.NONE.of(1.23451, 0.19).hashCode(), ValuePair.Name.NONE.of(1.23452, 0.19).hashCode());
    Assert.assertNotEquals(ValuePair.Name.NONE.of(1.3, 0.1).hashCode(), ValuePair.Name.NONE.of(1.23452, 0.1).hashCode());
  }

  @Test
  public void testMerge() {
    ValuePair v1 = ValuePair.Name.NONE.of(10.0, 1.0);
    ValuePair v2 = ValuePair.Name.NONE.of(30.0, 1.0);
    ValuePair v3 = ValuePair.Name.NONE.of(50.0, 1.0);
    ValuePair v4 = ValuePair.Name.NONE.of(30.0, 1.0);

    ValuePair merged = v1.mergeWith(v2).mergeWith(v3).mergeWith(v4);
    Assert.assertEquals(merged.getValue(), 30.0, 0.1, merged.toString());
    Assert.assertEquals(merged.getAbsError(), 0.5, 0.1, merged.toString());
  }
}