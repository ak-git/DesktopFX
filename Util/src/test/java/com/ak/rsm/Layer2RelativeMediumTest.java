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
            new Layer2RelativeMedium(1.0, Metrics.fromMilli(5.0)),
            new ValuePair[] {ValuePair.Name.K12.of(1.0, 0.0), ValuePair.Name.H_L.of(Metrics.fromMilli(5.0), 0.0)}
        },
        {
            new Layer2RelativeMedium(ValuePair.Name.NONE.of(1.0, 0.0), ValuePair.Name.NONE.of(Metrics.fromMilli(5.0), Metrics.fromMilli(0.1))),
            new ValuePair[] {ValuePair.Name.NONE.of(1.0, 0.0), ValuePair.Name.NONE.of(Metrics.fromMilli(5.0), Metrics.fromMilli(0.1))}
        },
    };
  }

  @Test(dataProvider = "layer2Medium")
  @ParametersAreNonnullByDefault
  public <T> void test(RelativeMediumLayers layers, ValuePair[] expected) {
    Assert.assertEquals(layers.k12(), expected[0].getValue());
    Assert.assertEquals(layers.k12AbsError(), expected[0].getAbsError());
    Assert.assertEquals(layers.hToL(), expected[1].getValue());
    Assert.assertEquals(layers.hToLAbsError(), expected[1].getAbsError());
  }

  @Test(dataProvider = "layer2Medium")
  @ParametersAreNonnullByDefault
  public <T> void testToString(RelativeMediumLayers layers, T[] expected) {
    Assert.assertTrue(layers.toString().contains(expected[0].toString()), layers.toString());
    Assert.assertTrue(layers.toString().contains(expected[1].toString()), layers.toString());
  }

  @Test(dataProvider = "layer2Medium")
  @ParametersAreNonnullByDefault
  public void testEquals(RelativeMediumLayers layers, ValuePair[] expected) {
    Assert.assertEquals(layers, layers, layers.toString());
    Assert.assertEquals(layers.hashCode(), layers.hashCode(), layers.toString());

    RelativeMediumLayers copy = new Layer2RelativeMedium(expected[0], expected[1]);
    Assert.assertEquals(layers, copy, layers.toString());
    Assert.assertEquals(copy, layers, layers.toString());

    Assert.assertNotEquals(layers, null, layers.toString());
    Assert.assertNotEquals(new Object(), layers, layers.toString());
    Assert.assertNotEquals(layers, new Object(), layers.toString());
  }
}