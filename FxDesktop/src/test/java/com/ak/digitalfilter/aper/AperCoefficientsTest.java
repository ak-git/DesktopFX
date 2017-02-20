package com.ak.digitalfilter.aper;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AperCoefficientsTest {
  private AperCoefficientsTest() {
  }

  @Test
  public static void testCoefficients() {
    for (AperCoefficients coefficients : AperCoefficients.values()) {
      Assert.assertEquals(coefficients.get().length, 61);
    }
  }
}