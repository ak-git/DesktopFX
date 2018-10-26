package com.ak.rsm;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

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
            new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).build(),
            new double[] {10.0, 1.0, Metrics.fromMilli(15.0), Metrics.fromMilli(0.01)},

            new double[] {34.420, 186.857, 34.399, 186.797}
        },
        {
            new TetrapolarSystemPair.Builder(0.1, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).buildWithError()[0],
            new double[] {10.0, 1.0, Metrics.fromMilli(15.0), Metrics.fromMilli(0.01)},

            new double[] {34.601, 183.855, 34.581, 183.795}
        },
        {
            new TetrapolarSystemPair.Builder(0.1, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).buildWithError()[1],
            new double[] {10.0, 1.0, Metrics.fromMilli(15.0), Metrics.fromMilli(0.01)},

            new double[] {34.237, 189.923, 34.216, 189.863}
        },
        {
            new TetrapolarSystemPair.Builder(0.1, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).buildWithError()[0],
            new double[] {1.0, 10.0, Metrics.fromMilli(10.0), Metrics.fromMilli(0.01)},

            new double[] {10.913, 39.527, 10.921, 39.553}
        },
        {
            new TetrapolarSystemPair.Builder(0.1, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0).buildWithError()[0],
            new double[] {1.0, 1.0, Metrics.fromMilli(15.0), Metrics.fromMilli(0.01)},

            new double[] {5.340, 23.558, 5.340, 23.558}
        }
    };
  }

  @Test(dataProvider = "noisy-resistance")
  public void testNoisyResistance(@Nonnull TetrapolarSystemPair system, @Nonnull double[] rho1rho2hDH,
                                  @Nonnull double[] rOhmExpected) {
    Assert.assertEquals(toString(new ResistanceTwoLayerPair(system).value(rho1rho2hDH)), toString(rOhmExpected));
  }

  @DataProvider(name = "inverseErrors")
  public static Object[][] inverseErrors() {
    return new Object[][] {
        {
            new TetrapolarSystemPair.Builder(0.1, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0),
            new double[] {10.0, 1.0, Metrics.fromMilli(5.0), Metrics.fromMilli(0.01)},

            new double[] {0.000, 0.048, 0.032, 0.021}
        },
        {
            new TetrapolarSystemPair.Builder(0.1, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0),
            new double[] {10.0, 1.0, Metrics.fromMilli(15.0), Metrics.fromMilli(0.01)},

            new double[] {0.043, 0.205, 0.066, 0.145}
        },
        {
            new TetrapolarSystemPair.Builder(0.1, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0),
            new double[] {10.0, 1.0, Metrics.fromMilli(25.0), Metrics.fromMilli(0.01)},

            new double[] {0.042, 0.037, 0.192, 0.801}
        },
        {
            new TetrapolarSystemPair.Builder(0.1, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0),
            new double[] {1.0, 10.0, Metrics.fromMilli(15.0), Metrics.fromMilli(0.01)},

            new double[] {0.061, 0.005, 0.098, 0.210}
        },
    };
  }

  @Test(dataProvider = "inverseErrors")
  public void testInverseErrors(@Nonnull TetrapolarSystemPair.Builder systemBuilder, @Nonnull double[] rho1rho2hDH,
                                @Nonnull double[] relErrorsExpected) {

    Arrays.stream(systemBuilder.buildWithError()).flatMap(systemPair -> {
      double[] rho1rho2hInverse = SimplexTest.optimizeBOBYQA(new MultivariateFunction() {
        private final double[] rOhmActual = new ResistanceTwoLayerPair(systemPair).value(rho1rho2hDH);
        private final ResistanceTwoLayerPair resistancePredicted = new ResistanceTwoLayerPair(systemBuilder.build());

        @Override
        public double value(double[] rho1rho2hDH) {
          return Inequality.logDifference().applyAsDouble(rOhmActual, resistancePredicted.value(rho1rho2hDH));
        }
      }, SimpleBounds.unbounded(rho1rho2hDH.length), rho1rho2hDH, 0.001).getPoint();

      double[] relErrors = new double[rho1rho2hInverse.length];
      for (int i = 0; i < rho1rho2hInverse.length; i++) {
        relErrors[i] = Inequality.proportional().applyAsDouble(rho1rho2hInverse[i], rho1rho2hDH[i]);
      }
      return Stream.of(relErrors);
    }).reduce((first, second) -> {
      double[] max = new double[first.length];
      for (int i = 0; i < max.length; i++) {
        max[i] = Math.max(first[i], second[i]);
      }
      return max;
    }).ifPresentOrElse(relErrors -> Assert.assertEquals(toString(relErrors), toString(relErrorsExpected)), Assert::fail);
  }

  @DataProvider(name = "experimental-errors")
  public static Object[][] experimentalErrors() {
    return new Object[][] {
        {
            new double[] {34.420, 186.857, 34.399, 186.797},
            new TetrapolarSystemPair.Builder(0.1, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0),
        },
        {
            new double[] {34.601, 183.855, 34.581, 183.795},
            new TetrapolarSystemPair.Builder(0.1, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0),
        },
        {
            new double[] {23.4, 111.5, 23.15, 110.6},
            new TetrapolarSystemPair.Builder(0.1, MILLI(METRE)).sPU(10.0, 30.0).lCC(50.0),
        },
    };
  }

  @Test(dataProvider = "experimental-errors", invocationCount = 10, enabled = false)
  public void testExperimentalErrors(@Nonnull double[] rOhmMeasured, @Nonnull TetrapolarSystemPair.Builder systemBuilder) {
    double rho2Apparent = systemBuilder.build().getPair()[0].getApparent(rOhmMeasured[0]);
    double rho1Apparent = systemBuilder.build().getPair()[1].getApparent(rOhmMeasured[1]);

    SimpleBounds simpleBounds = new SimpleBounds(
        new double[] {rho1Apparent, 0.0, 0.0, 0.0},
        new double[] {100.0, rho2Apparent, systemBuilder.getLCC() / 2.0, systemBuilder.getLCC() / 5.0}
    );

    double[] initialGuess = {rho1Apparent, rho2Apparent, systemBuilder.getLCC() / 3.0, systemBuilder.getLCC() / 5.0};

    Function<TetrapolarSystemPair, double[]> find = systemPair -> {
      ResistanceTwoLayerPair resistancePredicted = new ResistanceTwoLayerPair(systemPair);
      PointValuePair pair = getRho1Rho2H(rOhmMeasured, resistancePredicted, simpleBounds, initialGuess);
      return pair.getPoint();
    };

    double[] center = find.apply(systemBuilder.build());
    List<double[]> diff = Arrays.stream(systemBuilder.buildWithError()).map(find).collect(Collectors.toList());
    for (int i = 0; i < center.length; i++) {
      double min = Math.min(diff.get(0)[i], diff.get(1)[i]);
      double max = Math.max(diff.get(0)[i], diff.get(1)[i]);
      Logger.getAnonymousLogger().info(String.format("%.4f +%.4f/-%.4f", center[i], max - center[i], center[i] - min));
    }
  }

  private static PointValuePair getRho1Rho2H(@Nonnull double[] rOhmMeasured, @Nonnull ResistanceTwoLayerPair resistancePredicted,
                                             @Nonnull SimpleBounds simpleBounds, @Nonnull double[] rho1rho2hInitial) {
    return SimplexTest.optimizeCMAES(rho1rho2hDH -> Inequality.logDifference().applyAsDouble(rOhmMeasured, resistancePredicted.value(rho1rho2hDH)),
        simpleBounds, rho1rho2hInitial, new double[] {0.1, 0.1, Metrics.fromMilli(0.1), Metrics.fromMilli(0.01)}
    );
  }

  private static String toString(@Nonnull double[] doubles) {
    return DoubleStream.of(doubles).mapToObj(operand -> String.format("%.3f", operand))
        .collect(Collectors.joining(", ", "[", "]"));
  }
}