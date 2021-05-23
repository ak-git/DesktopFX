package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.util.Metrics;
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
                .map(m -> new TetrapolarPrediction(m, RelativeMediumLayers.SINGLE_LAYER, avg.getResistivity()))
                .collect(Collectors.toList()))
            .layer1(avg).build(), new ValuePair(0.0654, 0.00072)},
    };
  }

  @Test(dataProvider = "layer1Medium")
  @ParametersAreNonnullByDefault
  public void testRho(MediumLayers<ValuePair> layers, ValuePair expected) {
    Assert.assertEquals(layers.rho(), expected, expected.toString());
    Assert.assertEquals(layers.rho1(), expected, expected.toString());
    Assert.assertEquals(layers.rho2(), expected, expected.toString());
  }

  @Test(dataProvider = "layer1Medium")
  @ParametersAreNonnullByDefault
  public void testH(MediumLayers<ValuePair> layers, ValuePair expected) {
    Assert.assertTrue(Double.isNaN(layers.h1().getValue()));
    Assert.assertNotNull(expected);
  }

  @Test(dataProvider = "layer1Medium")
  @ParametersAreNonnullByDefault
  public void testToString(MediumLayers<ValuePair> layers, ValuePair expected) {
    Assert.assertTrue(layers.toString().contains(expected.toString()), layers.toString());
    double l2 = Arrays.stream(new double[] {0.3277112113340609, 0.10361494844541479, 0.5126497744983622, 0.6807219716648473})
        .reduce(StrictMath::hypot).orElse(Double.NaN);
    Assert.assertEquals(layers.getInequalityL2(), new double[] {l2}, 0.001, layers.toString());
    Assert.assertTrue(layers.toString().contains("[%.1f] %%".formatted(Metrics.toPercents(l2))), layers.toString());
  }
}