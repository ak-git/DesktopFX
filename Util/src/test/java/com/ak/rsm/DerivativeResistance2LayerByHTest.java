package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public class DerivativeResistance2LayerByHTest {
  private DerivativeResistance2LayerByHTest() {
  }

  @DataProvider(name = "layer-model")
  public static Object[][] twoLayerParameters() {
    return new Object[][] {
        {new TetrapolarSystem(0.5, 0.9, Units.METRE), 5.0, 1.0, 0.09, 20.242},
        {new TetrapolarSystem(500.0, 900.0, MetricPrefix.MILLI(Units.METRE)), 5.0, 1.0, 0.09 * Metrics.fromMilli(900.0), 19.163},
        {new TetrapolarSystem(50.0, 90.0, MetricPrefix.MILLI(Units.METRE)), 5.0, 1.0, 0.09 * Metrics.fromMilli(90.0), 1916.316},
        {new TetrapolarSystem(10.0, 30.0, MetricPrefix.MILLI(Units.METRE)), 0.7, Double.POSITIVE_INFINITY, Metrics.fromMilli(5.0), -6083.591},
    };
  }

  @Test(dataProvider = "layer-model")
  public static void testLayer(@Nonnull TetrapolarSystem system, @Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double hSI, double expected) {
    Assert.assertEquals(new DerivativeResistance2LayerByH(system).value(rho1, rho2, hSI), expected, 0.001);
  }
}