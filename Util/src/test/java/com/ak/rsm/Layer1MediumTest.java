package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.rsm.InexactTetrapolarSystem.systems4;

public class Layer1MediumTest {
  @DataProvider(name = "layer1Medium")
  public static Object[][] layer1Medium() {
    Collection<Measurement> measurements = TetrapolarMeasurement.of(systems4(0.1, 7.0), new double[] {1.0, 2.0, 3.0, 4.0});
    Measurement avg = measurements.stream().reduce(Measurement::merge).orElseThrow();
    return new Object[][] {
        {new Layer1Medium.Layer1MediumBuilder(
            measurements.stream()
                .map(m -> new TetrapolarPrediction(m, avg.getResistivity())).collect(Collectors.toList()))
            .layer1(avg).build(), 0.065},
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
    Assert.assertTrue(layers.toString().contains(Strings.rho(expected)), layers.toString());
    double l2 = Arrays.stream(new double[] {0.3277112113340609, 0.10361494844541479, 0.5126497744983622, 0.6807219716648473})
        .reduce(StrictMath::hypot).orElse(Double.NaN);
    Assert.assertTrue(layers.toString().contains("%.2f %%".formatted(Metrics.toPercents(l2))), layers.toString());
  }
}