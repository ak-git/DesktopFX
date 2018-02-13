package com.ak.rsm;

import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class ResistanceTwoLayerPairTest {
  private ResistanceTwoLayerPairTest() {
  }

  @DataProvider(name = "layer-model", parallel = true)
  public static Object[][] twoLayerParameters() {
    return new Object[][] {
        {new ResistanceTwoLayerPair(new TetrapolarSystemPair(10.0, 30.0, 50.0, MILLI(METRE)), Metrics.fromMilli(0.01)),
            new double[] {10.0, 1.0}, Metrics.fromMilli(15.0),

            new double[] {34.420, 186.857, 34.441, 186.917}
        },
    };
  }

  @Test(dataProvider = "layer-model")
  public void testValue(@Nonnull ResistanceTwoLayerPair layerPair, @Nonnull double[] rho1rho2, @Nonnegative double hSI, @Nonnull double[] rOhms) {
    Assert.assertEquals(toString(layerPair.value(rho1rho2[0], rho1rho2[1], hSI)), toString(rOhms));
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public static void testNotClone() throws CloneNotSupportedException {
    new ResistanceTwoLayerPair(new TetrapolarSystemPair(1, 2, 3, MILLI(METRE)), 0.1).clone();
  }

  private static String toString(@Nonnull double[] doubles) {
    return DoubleStream.of(doubles)
        .mapToObj(operand -> String.format("%.3f", operand))
        .collect(Collectors.joining(", ", "[", "]"));
  }
}