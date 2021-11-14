package com.ak.rsm;

import java.util.Collection;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
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
            new Layer2Medium(measurements, new Layer2RelativeMedium(
                ValuePair.Name.K12.of(0.6, 0.01),
                ValuePair.Name.H.of(0.2, 0.01))
            ),
            new double[] {0.0429, 0.1716, Metrics.fromMilli(7.0 * 4 * 0.2)}
        },
        {
            new Layer2Medium(measurements, new Layer2RelativeMedium(
                ValuePair.Name.K12.of(-0.6, 0.01),
                ValuePair.Name.H.of(0.3, 0.01))
            ),
            new double[] {0.0809, 0.020, Metrics.fromMilli(7.0 * 4 * 0.3)}
        },
        {
            new Layer2Medium(measurements, RelativeMediumLayers.SINGLE_LAYER),
            new double[] {0.0654, 0.065, Double.NaN}
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
    Assert.assertTrue(layers.toString().contains("%.4f".formatted(expected[0])), layers.toString());
    Assert.assertTrue(layers.toString().contains("%.1f".formatted(expected[1])), layers.toString());
    Assert.assertTrue(layers.toString().contains("%.1f".formatted(expected[2])), layers.toString());
  }
}