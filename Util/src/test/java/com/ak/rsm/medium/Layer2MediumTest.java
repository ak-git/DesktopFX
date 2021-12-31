package com.ak.rsm.medium;

import java.util.Collection;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Layer2MediumTest {
  @DataProvider(name = "layer2Medium")
  public static Object[][] layer2Medium() {
    Collection<Measurement> measurements = TetrapolarMeasurement.milli2(0.1, 7.0).ofOhms(1.0, 2.0);
    return new Object[][] {
        {
            new Layer2Medium(measurements, new Layer2RelativeMedium(
                ValuePair.Name.K12.of(0.6, 0.01),
                ValuePair.Name.H_L.of(0.2, 0.01))
            ),
            new double[] {0.0287, 0.1149, Metrics.fromMilli(7.0 * 3 * 0.2)}
        },
        {
            new Layer2Medium(measurements, new Layer2RelativeMedium(
                ValuePair.Name.K12.of(-0.6, 0.001),
                ValuePair.Name.H_L.of(0.1, 0.001))
            ),
            new double[] {0.1601, 0.0400, Metrics.fromMilli(7.0 * 3 * 0.1)}
        },
        {
            new Layer2Medium(measurements, RelativeMediumLayers.SINGLE_LAYER),
            new double[] {0.0522, 0.0522, Double.NaN}
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
