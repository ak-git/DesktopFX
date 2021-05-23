package com.ak.rsm;

import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.rsm.InexactTetrapolarSystem.systems4;

public class Layer2MediumTest {
  @DataProvider(name = "layer2Medium")
  public static Object[][] layer2Medium() {
    return new Object[][] {
        {new Layer2Medium.Layer2MediumBuilder<Double>(
            TetrapolarMeasurement.of(systems4(0.1, 7.0), new double[] {1.0, 2.0, 3.0, 4.0}).stream()
                .map(m -> new TetrapolarPrediction(m, RelativeMediumLayers.SINGLE_LAYER, 2.001))
                .collect(Collectors.toList()))
            .layer1(2.0012, Metrics.fromMilli(5.0)).layer2(10.999).build(),
            new double[] {2.001, 10.999, Metrics.fromMilli(5.0)}},
    };
  }

  @Test(dataProvider = "layer2Medium")
  @ParametersAreNonnullByDefault
  public void testRho(MediumLayers<Double> layers, double[] expected) {
    Assert.assertEquals(layers.rho(), expected[0], 0.001);
    Assert.assertEquals(layers.rho1(), expected[0], 0.001);
    Assert.assertEquals(layers.rho2(), expected[1], 0.001);
    Assert.assertEquals(layers.h1(), expected[2], 0.001);
  }

  @Test(dataProvider = "layer2Medium")
  @ParametersAreNonnullByDefault
  public void testToString(MediumLayers<Double> layers, double[] expected) {
    Assert.assertTrue(layers.toString().contains(Double.toString(expected[0])), layers.toString());
    Assert.assertTrue(layers.toString().contains(Double.toString(expected[1])), layers.toString());
    Assert.assertTrue(layers.toString().contains(Double.toString(expected[2])), layers.toString());
  }
}