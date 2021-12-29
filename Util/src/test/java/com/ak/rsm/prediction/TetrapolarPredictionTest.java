package com.ak.rsm.prediction;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.medium.Layer2RelativeMedium;
import com.ak.rsm.medium.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TetrapolarPredictionTest {
  @DataProvider(name = "predictions")
  public static Object[][] predictions() {
    Prediction prediction1 = TetrapolarPrediction.of(new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 20.0)),
        new Layer2RelativeMedium(0.5, 0.5), 10.0, 100.0);
    Prediction prediction2 = TetrapolarPrediction.of(new InexactTetrapolarSystem(0.1, new TetrapolarSystem(20.0, 10.0)),
        new Layer2RelativeMedium(0.5, 1.0), 10.0, 100.0);

    Prediction prediction3 = TetrapolarPrediction.of(new InexactTetrapolarSystem(0.3, new TetrapolarSystem(10.0, 20.0)),
        new Layer2RelativeMedium(0.5, 0.5), 20.0, 10.0);
    return new Object[][] {
        {prediction1, prediction1, true},
        {prediction1, prediction2, true},
        {prediction1, prediction3, false},
        {prediction1, new Object(), false},
        {new Object(), prediction1, false},
        {new AbstractPrediction(0.0, new double[] {0.0}) {
          @Nonnull
          @Override
          public double[] getHorizons() {
            return getInequalityL2();
          }
        }, new Object(), false},
    };
  }

  @Test(dataProvider = "predictions")
  @ParametersAreNonnullByDefault
  public void testEquals(Object o1, Object o2, boolean equals) {
    Assert.assertEquals(o1.equals(o2), equals, "%s compared with %s".formatted(o1, o2));
    Assert.assertEquals(o1.hashCode() == o2.hashCode(), equals, "%s compared with %s".formatted(o1, o2));
    Assert.assertNotEquals(o1, null);
  }

  @Test
  public void testPrediction() {
    Prediction prediction = TetrapolarPrediction.of(new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 20.0)),
        RelativeMediumLayers.SINGLE_LAYER, 10.0, 100.0);
    Assert.assertEquals(prediction.getHorizons(), new double[] {Double.POSITIVE_INFINITY, 0.0}, prediction.toString());
    Assert.assertEquals(prediction.getResistivityPredicted(), 10.0, 0.001, prediction.toString());
    Assert.assertEquals(prediction.getInequalityL2(), new double[] {9.0}, 0.001, prediction.toString());
  }

  @Test
  public void testMergeHorizons() {
    Prediction prediction1 = TetrapolarPrediction.of(new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 30.0)),
        new Layer2RelativeMedium(0.5, 0.5), 10.0, 100.0);
    Prediction prediction2 = TetrapolarPrediction.of(new InexactTetrapolarSystem(0.2, new TetrapolarSystem(50.0, 30.0)),
        new Layer2RelativeMedium(0.5, 0.5), 10.0, 100.0);

    List<Prediction> predictions = List.of(prediction1, prediction2);
    Assert.assertEquals(prediction1.getHorizons(), new double[] {0.378, 27.210}, 0.001, prediction1.toString());
    Assert.assertEquals(prediction2.getHorizons(), new double[] {0.504, 36.929}, 0.001, prediction2.toString());
    Assert.assertEquals(AbstractPrediction.mergeHorizons(predictions), new double[] {0.504, 27.210}, 0.001,
        predictions.toString());
  }
}