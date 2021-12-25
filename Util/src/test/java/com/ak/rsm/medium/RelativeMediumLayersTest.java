package com.ak.rsm.medium;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RelativeMediumLayersTest {
  @Test
  public void testSingleLayer() {
    Assert.assertEquals(RelativeMediumLayers.SINGLE_LAYER.k12(), 0.0, 0.1);
    Assert.assertEquals(RelativeMediumLayers.SINGLE_LAYER.k12AbsError(), 0.0, 0.1);
    Assert.assertEquals(RelativeMediumLayers.SINGLE_LAYER.hToL(), Double.NaN, 0.1);
    Assert.assertEquals(RelativeMediumLayers.SINGLE_LAYER.hToLAbsError(), 0.0, 0.1);
    Assert.assertTrue(RelativeMediumLayers.SINGLE_LAYER.toString().isEmpty());
  }

  @Test
  public void testNaN() {
    Assert.assertEquals(RelativeMediumLayers.NAN.k12(), Double.NaN, 0.1);
    Assert.assertEquals(RelativeMediumLayers.NAN.k12AbsError(), 0.0, 0.1);
    Assert.assertEquals(RelativeMediumLayers.NAN.hToL(), Double.NaN, 0.1);
    Assert.assertEquals(RelativeMediumLayers.NAN.hToLAbsError(), 0.0, 0.1);
    Assert.assertEquals(RelativeMediumLayers.NAN.toString(), String.valueOf(Double.NaN));
  }
}