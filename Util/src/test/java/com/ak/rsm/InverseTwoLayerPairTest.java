package com.ak.rsm;

import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.SimplexTest;
import com.ak.util.Metrics;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class InverseTwoLayerPairTest {
  private InverseTwoLayerPairTest() {
  }

  @DataProvider(name = "noisy-resistance")
  public static Object[][] noisyResistance() {
    return new Object[][] {
        {
            new TetrapolarSystemPair.Builder(MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).build(),
            new double[] {10.0, 1.0, Metrics.fromMilli(15.0)}, Metrics.fromMilli(0.01),

            new double[] {34.420, 186.857, 34.399, 186.797}
        },
        {
            new TetrapolarSystemPair.Builder(MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).buildWithError(0.1),
            new double[] {10.0, 1.0, Metrics.fromMilli(15.0)}, Metrics.fromMilli(0.01),

            new double[] {34.601, 183.855, 34.581, 183.795}
        },
        {
            new TetrapolarSystemPair.Builder(MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).buildWithError(0.1),
            new double[] {1.0, 10.0, Metrics.fromMilli(10.0)}, Metrics.fromMilli(0.01),

            new double[] {10.913, 39.527, 10.921, 39.553}
        },
        {
            new TetrapolarSystemPair.Builder(MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).buildWithError(0.1),
            new double[] {1.0, 1.0, Metrics.fromMilli(15.0)}, Metrics.fromMilli(0.01),

            new double[] {5.340, 23.558, 5.340, 23.558}
        }
    };
  }

  @Test(dataProvider = "noisy-resistance")
  public void testNoisyResistance(@Nonnull TetrapolarSystemPair system, @Nonnull double[] rho1rho2h, @Nonnegative double dH,
                                  @Nonnull double[] rOhmExpected) {
    Assert.assertEquals(toString(new ResistanceTwoLayerPair(system, dH).value(rho1rho2h)), toString(rOhmExpected));
  }

  @DataProvider(name = "inverseErrors")
  public static Object[][] inverseErrors() {
    return new Object[][] {
        {
            new TetrapolarSystemPair.Builder(MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0),
            new double[] {10.0, 1.0, Metrics.fromMilli(5.0)}, Metrics.fromMilli(0.01),

            new double[] {0.004, 0.040, 0.027}
        },
        {
            new TetrapolarSystemPair.Builder(MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0),
            new double[] {10.0, 1.0, Metrics.fromMilli(15.0)}, Metrics.fromMilli(0.01),

            new double[] {0.040, 0.120, 0.064}
        },
        {
            new TetrapolarSystemPair.Builder(MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0),
            new double[] {10.0, 1.0, Metrics.fromMilli(25.0)}, Metrics.fromMilli(0.01),

            new double[] {0.042, 0.139, 0.204}
        },
        {
            new TetrapolarSystemPair.Builder(MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0),
            new double[] {1.0, 10.0, Metrics.fromMilli(15.0)}, Metrics.fromMilli(0.01),

            new double[] {0.061, 0.002, 0.089}
        },
    };
  }

  @Test(dataProvider = "inverseErrors")
  public void testInverseErrors(@Nonnull TetrapolarSystemPair.Builder systemBuilder, @Nonnull double[] rho1rho2h, @Nonnegative double dH,
                                @Nonnull double[] relErrorsExpected) {
    double[] rho1rho2hInverse = SimplexTest.optimizeBOBYQA(new MultivariateFunction() {
      private final double[] rOhmActual = new ResistanceTwoLayerPair(systemBuilder.buildWithError(0.1), dH).value(rho1rho2h);
      private final ResistanceTwoLayerPair resistancePredicted = new ResistanceTwoLayerPair(systemBuilder.build(), dH);

      @Override
      public double value(double[] rho1rho2h) {
        return Inequality.logDifference().applyAsDouble(rOhmActual, resistancePredicted.value(rho1rho2h));
      }
    }, SimpleBounds.unbounded(rho1rho2h.length), rho1rho2h, 0.001).getPoint();

    double[] relErrors = new double[rho1rho2hInverse.length];
    for (int i = 0; i < rho1rho2hInverse.length; i++) {
      relErrors[i] = Inequality.proportional().applyAsDouble(rho1rho2hInverse[i], rho1rho2h[i]);
    }
    Assert.assertEquals(toString(relErrors), toString(relErrorsExpected),
        String.format("Forward problem: %s, Inverse: %s", toString(rho1rho2h), toString(rho1rho2hInverse)));
  }

  @DataProvider(name = "experimental-errors")
  public static Object[][] experimentalErrors() {
    return new Object[][] {
        {
            new double[] {34.420, 186.857, 34.399, 186.797},
            new TetrapolarSystemPair.Builder(MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0),
            Metrics.fromMilli(0.01),

            new double[] {10.011, 1.236, Metrics.fromMilli(15.0)}
        },
        {
            new double[] {34.601, 183.855, 34.581, 183.795},
            new TetrapolarSystemPair.Builder(MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0),
            Metrics.fromMilli(0.01),

            new double[] {9.749, 3.098, Metrics.fromMilli(12.0)}
        },
    };
  }

  @Test(dataProvider = "experimental-errors", invocationCount = 10, enabled = false)
  public void testExperimentalErrors(@Nonnull double[] rOhmMeasured, @Nonnull TetrapolarSystemPair.Builder systemBuilder,
                                     @Nonnegative double dH, @Nonnull double[] rho1rho2hExpected) {
    double rho2Apparent = systemBuilder.build().getPair()[0].getApparent(rOhmMeasured[0]);
    double rho1Apparent = systemBuilder.build().getPair()[1].getApparent(rOhmMeasured[1]);

    SimpleBounds simpleBounds = new SimpleBounds(
        new double[] {rho1Apparent, 0.0, 0.0},
        new double[] {100.0, rho2Apparent, systemBuilder.getLCC() / 2.0}
    );
    if (rho1Apparent <= rho2Apparent) {
      throw new UnsupportedOperationException();
    }

    PointValuePair pair = SimplexTest.optimizeCMAES(new MultivariateFunction() {
      private final ResistanceTwoLayerPair resistancePredicted = new ResistanceTwoLayerPair(systemBuilder.build(), dH);

      @Override
      public double value(double[] rho1rho2h) {
        return Inequality.logDifference().applyAsDouble(rOhmMeasured, resistancePredicted.value(rho1rho2h));
      }
    }, simpleBounds, new double[] {rho1Apparent, rho2Apparent, systemBuilder.getLCC() / 3.0}, new double[] {0.1, 0.1, Metrics.fromMilli(0.1)});
    Assert.assertEquals(toString(pair.getPoint()), toString(rho1rho2hExpected));
  }

  private static String toString(@Nonnull double[] doubles) {
    return DoubleStream.of(doubles).mapToObj(operand -> String.format("%.3f", operand))
        .collect(Collectors.joining(", ", "[", "]"));
  }
}