package com.ak.rsm;

import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

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

  @DataProvider(name = "layer-model")
  public static Object[][] twoLayerParameters() {
    return new Object[][] {
        {new ResistanceTwoLayerPair(new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).build(), Metrics.fromMilli(0.01)),
            new double[] {10.0, 1.0, Metrics.fromMilli(15.0)},

            new double[] {34.420, 186.857, 34.399, 186.797}
        },
    };
  }

  @Test(dataProvider = "layer-model")
  public void testValue(@Nonnull ResistanceTwoLayerPair layerPair, @Nonnull double[] rho1rho2h, @Nonnull double[] rOhms) {
    Assert.assertEquals(toString(layerPair.value(rho1rho2h)), toString(rOhms));
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public static void testNotClone() throws CloneNotSupportedException {
    new ResistanceTwoLayerPair(new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(1, 2).lCC(3).build(), 0.1).clone();
  }

  private static String toString(@Nonnull double[] doubles) {
    return DoubleStream.of(doubles)
        .mapToObj(operand -> String.format("%.3f", operand))
        .collect(Collectors.joining(", ", "[", "]"));
  }
}