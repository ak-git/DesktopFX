package com.ak.rsm;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Layer2RelativeMediumTest {
  @DataProvider(name = "layer2Medium")
  public static Object[][] layer2Medium() {
    return new Object[][] {
        {
            new Layer2RelativeMedium<>(1.0, Metrics.fromMilli(5.0)),
            new Double[] {1.0, Metrics.fromMilli(5.0)}
        },
        {
            new Layer2RelativeMedium<>(new ValuePair(1.0), new ValuePair(Metrics.fromMilli(5.0), Metrics.fromMilli(0.1))),
            new ValuePair[] {new ValuePair(1.0), new ValuePair(Metrics.fromMilli(5.0), Metrics.fromMilli(0.1))}
        },
    };
  }

  @Test(dataProvider = "layer2Medium")
  @ParametersAreNonnullByDefault
  public <T> void test(RelativeMediumLayers<T> layers, T[] expected) {
    Assert.assertEquals(layers.k12(), expected[0]);
    Assert.assertEquals(layers.h(), expected[1]);
  }

  @Test(dataProvider = "layer2Medium")
  @ParametersAreNonnullByDefault
  public <T> void testToString(RelativeMediumLayers<T> layers, T[] expected) {
    Assert.assertTrue(layers.toString().contains(expected[0].toString()), layers.toString());
    Assert.assertTrue(layers.toString().contains(expected[1].toString()), layers.toString());
  }
}