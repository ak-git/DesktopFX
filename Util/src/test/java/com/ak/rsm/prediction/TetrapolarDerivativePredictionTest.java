package com.ak.rsm.prediction;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.system.TetrapolarSystem;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TetrapolarDerivativePredictionTest {
  @DataProvider(name = "predictions")
  public static Object[][] predictions() {
    Prediction prediction1 = TetrapolarDerivativePrediction.of(
        new DerivativeResistivity() {
          @Override
          public double derivativeResistivity() {
            return 10.0;
          }

          @Override
          public double dh() {
            return Double.NaN;
          }

          @Override
          public double resistivity() {
            return 100.0;
          }

          @Nonnull
          @Override
          public TetrapolarSystem system() {
            return new TetrapolarSystem(10.0, 20.0);
          }
        },
        new Layer2RelativeMedium(0.5, 0.5), 10.0);
    Prediction prediction2 = TetrapolarDerivativePrediction.of(
        new DerivativeResistivity() {
          @Override
          public double derivativeResistivity() {
            return 10.0;
          }

          @Override
          public double dh() {
            return Double.NaN;
          }

          @Override
          public double resistivity() {
            return 100.0;
          }

          @Nonnull
          @Override
          public TetrapolarSystem system() {
            return new TetrapolarSystem(10.0, 20.0);
          }
        },
        new Layer2RelativeMedium(0.5, 0.5), 10.0);

    Prediction prediction3 = TetrapolarDerivativePrediction.of(
        new DerivativeResistivity() {
          @Override
          public double derivativeResistivity() {
            return 10.0;
          }

          @Override
          public double dh() {
            return Double.NaN;
          }

          @Override
          public double resistivity() {
            return 100.0;
          }

          @Nonnull
          @Override
          public TetrapolarSystem system() {
            return new TetrapolarSystem(20.0, 10.0);
          }
        },
        new Layer2RelativeMedium(0.5, 1.0), 10.0);
    return new Object[][] {
        {prediction1, prediction1, true},
        {prediction1, prediction2, true},
        {prediction1, prediction3, false},
        {prediction1, new Object(), false},
        {new Object(), prediction1, false},
        {
            new AbstractPrediction(0.0, new double[] {0.0}) {
            },
            new Object(), false
        },
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
    Prediction prediction = TetrapolarDerivativePrediction.of(
        new DerivativeResistivity() {
          @Override
          public double derivativeResistivity() {
            return 101.0;
          }

          @Override
          public double dh() {
            return Double.NaN;
          }

          @Override
          public double resistivity() {
            return 100.0;
          }

          @Nonnull
          @Override
          public TetrapolarSystem system() {
            return new TetrapolarSystem(10.0, 20.0);
          }
        },
        new Layer2RelativeMedium(1.0, 1.0), 10.0);
    Assert.assertEquals(prediction.getPredicted(), -0.723, 0.001, prediction.toString());
    Assert.assertEquals(prediction.getInequalityL2(), new double[] {8.75, 140.766}, 0.001, prediction.toString());
  }
}