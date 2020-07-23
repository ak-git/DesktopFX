package com.ak.rsm;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Layer2RelativeMediumTest {
  @DataProvider(name = "layer2Medium")
  public static Object[][] layer2Medium() {
    return new Object[][] {
        {new Layer2RelativeMedium(1.0, Metrics.fromMilli(5.0)),
            new double[] {1.0, Metrics.fromMilli(5.0)}},
    };
  }

  @Test(dataProvider = "layer2Medium")
  @ParametersAreNonnullByDefault
  public void test(RelativeMediumLayers layers, double[] expected) {
    Assert.assertEquals(layers.k12(), expected[0], 0.001);
    Assert.assertEquals(layers.h(), expected[1], 0.001);
  }

  @Test(dataProvider = "layer2Medium")
  @ParametersAreNonnullByDefault
  public void testToString(RelativeMediumLayers layers, double[] expected) {
    Assert.assertTrue(layers.toString().contains(String.format("%.3f", expected[0])), layers.toString());
    Assert.assertTrue(layers.toString().contains(Strings.h(expected[1], 1)), layers.toString());
  }
}