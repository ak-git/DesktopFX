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
  private static final double[] EMPTY = {};

  private ResistanceTwoLayerPairTest() {
  }

  @DataProvider(name = "layer-model")
  public static Object[][] twoLayerParameters() {
    return new Object[][] {
        {
            new ResistanceTwoLayerPair(new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).build()),
            new double[] {10.0, 1.0, Metrics.fromMilli(15.0), Metrics.fromMilli(0.01)},

            new double[] {34.420, 186.857, 34.399, 186.797}
        },
        {
            new ResistanceTwoLayerPair(new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).build())
                .dh(Metrics.fromMilli(0.01)),
            new double[] {10.0, 1.0, Metrics.fromMilli(15.0)},

            new double[] {34.420, 186.857, 34.399, 186.797}
        },
        {
            new ResistanceTwoLayerPair(new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).build())
                .h(Metrics.fromMilli(15.0))
                .dh(Metrics.fromMilli(0.01)),
            new double[] {10.0, 1.0},

            new double[] {34.420, 186.857, 34.399, 186.797}
        },
        {
            new ResistanceTwoLayerPair(new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).build())
                .rho2(1.0)
                .h(Metrics.fromMilli(15.0))
                .dh(Metrics.fromMilli(0.01)),
            new double[] {10.0},

            new double[] {34.420, 186.857, 34.399, 186.797}
        },
        {
            new ResistanceTwoLayerPair(new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).build())
                .rho1(10.0)
                .rho2(1.0)
                .h(Metrics.fromMilli(15.0))
                .dh(Metrics.fromMilli(0.01)),
            EMPTY,

            new double[] {34.420, 186.857, 34.399, 186.797}
        },
    };
  }

  @Test(dataProvider = "layer-model")
  public void testValue(@Nonnull ResistanceTwoLayerPair layerPair, @Nonnull double[] params, @Nonnull double[] rOhms) {
    if (params.length == 0) {
      Assert.assertEquals(toString(layerPair.value()), toString(rOhms));
    }
    else {
      Assert.assertEquals(toString(layerPair.value(params)), toString(rOhms));
    }
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public static void testNotClone() throws CloneNotSupportedException {
    new ResistanceTwoLayerPair(new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(1, 2).lCC(3).build()).clone();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testInvalidParams() {
    new ResistanceTwoLayerPair(new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(1, 2).lCC(3).build()).value(new double[] {1, 2, 3, 4, 5});
  }

  private static String toString(@Nonnull double[] doubles) {
    return DoubleStream.of(doubles)
        .mapToObj(operand -> String.format("%.3f", operand))
        .collect(Collectors.joining(", ", "[", "]"));
  }
}