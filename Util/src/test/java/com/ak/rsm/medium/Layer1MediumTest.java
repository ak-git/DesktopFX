package com.ak.rsm.medium;

import java.util.Arrays;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Layer1MediumTest {
  @DataProvider(name = "layer1Medium")
  public static Object[][] layer1Medium() {
    return new Object[][] {
        {
            new Layer1Medium(TetrapolarMeasurement.milli(0.1).system4(7.0).ofOhms(1.0, 2.0, 3.0, 4.0)),
            ValuePair.Name.RHO_1.of(0.0654, 0.00072)
        },
    };
  }

  @Test(dataProvider = "layer1Medium")
  @ParametersAreNonnullByDefault
  public void testRho(MediumLayers layers, ValuePair expected) {
    Assert.assertEquals(layers.rho(), expected, layers.toString());
    Assert.assertEquals(layers.rho1(), expected, layers.toString());
    Assert.assertEquals(layers.rho2(), expected, layers.toString());
  }

  @Test(dataProvider = "layer1Medium")
  @ParametersAreNonnullByDefault
  public void testH(MediumLayers layers, ValuePair expected) {
    Assert.assertTrue(Double.isNaN(layers.h1().getValue()));
    Assert.assertNotNull(expected);
  }

  @Test(dataProvider = "layer1Medium")
  @ParametersAreNonnullByDefault
  public void testToString(MediumLayers layers, ValuePair expected) {
    Assert.assertTrue(layers.toString().contains(expected.toString()), layers.toString());
    double[] array = {0.3277112113340609, 0.10361494844541479, 0.5126497744983622, 0.6807219716648473};
    double rms = Arrays.stream(array).reduce(StrictMath::hypot).orElse(Double.NaN) / Math.sqrt(array.length);
    Assert.assertEquals(layers.getRMS(), new double[] {rms}, 0.001, layers.toString());
    Assert.assertTrue(layers.toString().contains("[%.1f] %%".formatted(Metrics.toPercents(rms))), layers.toString());
  }
}