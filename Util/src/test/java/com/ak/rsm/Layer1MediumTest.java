package com.ak.rsm;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Layer1MediumTest {
  @DataProvider(name = "layer1Medium")
  public static Object[][] layer1Medium() {
    return new Object[][] {
        {new Layer1Medium.Layer1MediumBuilder(
            TetrapolarMeasurement.of(LayersProvider.systems4(7.0), new double[] {1.0, 2.0, 3.0, 4.0}).stream()
                .map(m -> new TetrapolarPrediction(m, 0.044)).collect(Collectors.toList()))
            .layer1(0.044).build(), 0.044},
    };
  }

  @Test(dataProvider = "layer1Medium")
  public void testRho(@Nonnull MediumLayers layers, @Nonnegative double expected) {
    Assert.assertEquals(layers.rho(), expected, 0.001);
  }

  @Test(dataProvider = "layer1Medium")
  public void testRho1(@Nonnull MediumLayers layers, @Nonnegative double expected) {
    Assert.assertEquals(layers.rho1(), expected, 0.001);
  }

  @Test(dataProvider = "layer1Medium")
  public void testRho2(@Nonnull MediumLayers layers, @Nonnegative double expected) {
    Assert.assertEquals(layers.rho2(), expected, 0.001);
  }

  @Test(dataProvider = "layer1Medium")
  public void testH(@Nonnull RelativeMediumLayers layers, @Nonnegative double expected) {
    Assert.assertEquals(layers.k12(), 0.0, 0.001, layers.toString());
    Assert.assertTrue(Double.isNaN(layers.h()));
    Assert.assertTrue(expected > 0);
  }

  @Test(dataProvider = "layer1Medium")
  public void testToString(@Nonnull MediumLayers layers, @Nonnegative double expected) {
    Assert.assertTrue(layers.toString().startsWith(Strings.rho(expected)), layers.toString());
    double l2 = Arrays.stream(new double[] {0.000018, 0.014643, 0.054960, 0.065956}).reduce(StrictMath::hypot).orElse(Double.NaN);
    Assert.assertTrue(layers.toString().contains(String.format("%.2f %%", Metrics.toPercents(l2))), layers.toString());
  }
}