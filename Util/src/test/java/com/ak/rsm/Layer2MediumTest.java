package com.ak.rsm;

import java.util.Collection;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.rsm.TetrapolarSystem.systems4;

public class Layer2MediumTest {
  @DataProvider(name = "layer2Medium")
  public static Object[][] layer2Medium() {
    Collection<Measurement> measurements = TetrapolarMeasurement.of(systems4(0.1, 7.0), new double[] {1.0, 2.0, 3.0, 4.0});
    return new Object[][] {
        {
            new Layer2Medium(measurements, new Layer2RelativeMedium(0.6, 0.2)),
            new double[] {0.0469, 0.1879, Metrics.fromMilli(7.0 * 4 * 0.2)}
        },
        {
            new Layer2Medium(measurements, new Layer2RelativeMedium(-0.6, 0.1)),
            new double[] {0.1978, 0.0494, Metrics.fromMilli(7.0 * 4 * 0.1)}
        },
        {
            new Layer2Medium(measurements, RelativeMediumLayers.SINGLE_LAYER),
            new double[] {0.065, 0.065, Double.NaN}
        },
    };
  }

  @Test(dataProvider = "layer2Medium")
  @ParametersAreNonnullByDefault
  public void testRho(MediumLayers layers, double[] expected) {
    Assert.assertEquals(layers.rho().getValue(), expected[0], 0.001);
    Assert.assertEquals(layers.rho1().getValue(), expected[0], 0.001);
    Assert.assertEquals(layers.rho2().getValue(), expected[1], 0.001);
    Assert.assertEquals(layers.h1().getValue(), expected[2], 0.001);
  }

  @Test(dataProvider = "layer2Medium")
  @ParametersAreNonnullByDefault
  public void testToString(MediumLayers layers, double[] expected) {
    Assert.assertTrue(layers.toString().contains(Double.toString(expected[0])), layers.toString());
    Assert.assertTrue(layers.toString().contains(Double.toString(expected[1])), layers.toString());
    Assert.assertTrue(layers.toString().contains(Double.toString(expected[2])), layers.toString());
  }
}