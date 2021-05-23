package com.ak.rsm;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RelativeMediumLayersTest {
  @Test
  public void testK12() {
    Assert.assertEquals(RelativeMediumLayers.SINGLE_LAYER.k12(), 0.0, 0.1);
  }

  @Test
  public void testH() {
    Assert.assertEquals(RelativeMediumLayers.SINGLE_LAYER.hToL(), Double.NaN, 0.1);
  }
}