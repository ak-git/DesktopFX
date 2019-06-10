package com.ak.rsm;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.function.DoubleFunction;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.SimplexTest;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class Resistance2LayerTest {
  private Resistance2LayerTest() {
  }

  @DataProvider(name = "layer-model")
  public static Object[][] twoLayerParameters() {
    return new Object[][] {
        {new double[] {8.0, 1.0}, 10.0, 10.0, 20.0, 309.342},
        {new double[] {8.0, 1.0}, 10.0, 30.0, 90.0, 8.815},
        {new double[] {8.0, 1.0}, 50.0, 10.0, 20.0, 339.173},
        {new double[] {8.0, 1.0}, 50.0, 30.0, 90.0, 38.858},

        {new double[] {1.0, 1.0}, 0.0, 20.0, 40.0, 21.221},
        {new double[] {2.0, 2.0}, 0.0, 20.0, 40.0, 21.221 * 2.0},
        {new double[] {1.0, 1.0}, 0.0, 40.0, 80.0, 10.610},
        {new double[] {0.5, 0.5}, 0.0, 40.0, 80.0, 10.610 / 2.0},

        {new double[] {3.5, 1.35}, 10.0, 20.0, 40.0, 59.108},
        {new double[] {5.0, 2.0}, 15.0, 20.0, 40.0, 95.908},
        {new double[] {7.0, 1.0}, 20.0, 40.0, 80.0, 50.132},
        {new double[] {9.5, 0.5}, 30.0, 40.0, 80.0, 81.831},

        {new double[] {20.0, 1.0}, 1.0, 40.0, 80.0, 10.649},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0 - 10.0 / 200.0, 10.0, 30.0, 31.278},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0, 10.0, 30.0, 30.971},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0 - 10.0 / 200.0, 30.0, 50.0, 62.479},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0, 30.0, 50.0, 61.860},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0 - 10.0 / 200.0, 10.0, 50.0, 18.252},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0, 10.0, 50.0, 18.069},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0 - 10.0 / 200.0, 10.0, 30.0, 16.821},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0, 10.0, 30.0, 16.761},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0 - 10.0 / 200.0, 30.0, 50.0, 32.383},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0, 30.0, 50.0, 32.246},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0 - 10.0 / 200.0, 10.0, 50.0, 9.118},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0, 10.0, 50.0, 9.074},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0 - 10.0 / 200.0, 10.0, 30.0, 13.357},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0, 10.0, 30.0, 13.338},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0 - 10.0 / 200.0, 30.0, 50.0, 23.953},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0, 30.0, 50.0, 23.903},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0 - 10.0 / 200.0, 10.0, 50.0, 6.284},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0, 10.0, 50.0, 6.267},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0 - 10.0 / 200.0, 10.0, 30.0, 12.194},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0, 10.0, 30.0, 12.187},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0 - 10.0 / 200.0, 30.0, 50.0, 20.589},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0, 30.0, 50.0, 20.567},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0 - 10.0 / 200.0, 10.0, 50.0, 5.090},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0, 10.0, 50.0, 5.082},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0 - 10.0 / 200.0, 10.0, 30.0, 11.714},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0, 10.0, 30.0, 11.710},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0 - 10.0 / 200.0, 30.0, 50.0, 18.998},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0, 30.0, 50.0, 18.986},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0 - 10.0 / 200.0, 10.0, 50.0, 4.518},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0, 10.0, 50.0, 4.514},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0 - 10.0 / 200.0, 10.0, 30.0, 11.484},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0, 10.0, 30.0, 11.482},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0 - 10.0 / 200.0, 30.0, 50.0, 18.158},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0, 30.0, 50.0, 18.152},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0 - 10.0 / 200.0, 10.0, 50.0, 4.218},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0, 10.0, 50.0, 4.216},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0 - 10.0 / 200.0, 10.0, 30.0, 11.362},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0, 10.0, 30.0, 11.361},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0 - 10.0 / 200.0, 30.0, 50.0, 17.678},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0, 30.0, 50.0, 17.674},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0 - 10.0 / 200.0, 10.0, 50.0, 4.048},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0, 10.0, 50.0, 4.047},
    };
  }

  @Test(dataProvider = "layer-model")
  public static void testLayer(double[] rho, double hmm, double smm, double lmm, double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    Assert.assertEquals(new Resistance2Layer(system).value(rho[0], rho[1], Metrics.fromMilli(hmm)), rOhm, 0.001);
  }

  @Test(dataProviderClass = LayersProvider.class, dataProvider = "staticParameters", enabled = false)
  public static void testInverseStatic(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms) {
    Resistance2Layer[] predicted = Stream.of(systems).map(Resistance2Layer::new).toArray(Resistance2Layer[]::new);
    SimpleBounds bounds = new SimpleBounds(
        new double[] {0.0, 0.0, 0.0},
        new double[] {1000.0, 1000.0, Metrics.fromMilli(100.0)}
    );
    PointValuePair pair = SimplexTest.optimizeCMAES(point ->
            Inequality.absolute().applyAsDouble(rOhms, i -> predicted[i].value(point[0], point[1], point[2])),
        bounds, new double[] {0.01, 0.01, Metrics.fromMilli(0.1)});
    Logger.getAnonymousLogger().warning(toString3(pair, systems.length));
  }

  @Test(dataProviderClass = LayersProvider.class, dataProvider = "waterDynamicParameters2", enabled = false)
  public static void testInverseDynamic(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
    TrivariateFunction[] predicted = Stream.of(systems).map(Resistance2Layer::new).toArray(Resistance2Layer[]::new);
    TrivariateFunction[] predictedDiff = Stream.of(systems).map(DerivativeResistance2LayerByH::new).toArray(DerivativeResistance2LayerByH[]::new);

    DoubleSummaryStatistics apparent = IntStream.range(0, systems.length).mapToDouble(i -> new Resistance1Layer(systems[i]).getApparent(rOhmsBefore[i])).summaryStatistics();

    DoubleFunction<PointValuePair> rho1rho2 = h -> SimplexTest.optimizeNelderMead(rho -> {
      double rho1 = rho[0];
      double rho2 = rho[1];
      return Inequality.proportional().applyAsDouble(rOhmsBefore, i -> predicted[i].value(rho1, rho2, h));
    }, new double[] {apparent.getMin(), apparent.getMax()}, new double[] {0.1, 0.1});

    PointValuePair hPoint = SimplexTest.optimizeNelderMead(p -> {
      double h = p[0];
      double[] dH = new double[rOhmsBefore.length];
      Arrays.setAll(dH, i -> (rOhmsAfter[i] - rOhmsBefore[i]) / dh);

      PointValuePair pair = rho1rho2.apply(h);
      double rho1 = pair.getPoint()[0];
      double rho2 = pair.getPoint()[1];
      Inequality inequality = Inequality.absolute();
      inequality.applyAsDouble(dH, i -> predictedDiff[i].value(rho1, rho2, h));
      Logger.getAnonymousLogger().config(
          String.format("%s1 = %.3f, %s2 = %.3f, h = %.3f mm, e = %.6f, eh = %.6f", Strings.RHO, rho1, Strings.RHO, rho2, h * 1000, pair.getValue(), inequality.getAsDouble())
      );
      return inequality.getAsDouble();
    }, new double[] {Metrics.fromMilli(1.0)}, new double[] {Metrics.fromMilli(0.1)});

    double h = hPoint.getPoint()[0];
    PointValuePair rho = rho1rho2.apply(h);
    Logger.getAnonymousLogger().info(
        String.format("%s1 = %.3f, %s2 = %.3f, h = %.3f mm, e = %.6f, eh = %.6f",
            Strings.RHO, rho.getPoint()[0], Strings.RHO, rho.getPoint()[1], h * 1000, rho.getValue(), hPoint.getValue()
        )
    );
  }

  private static String toString3(@Nonnull PointValuePair point, @Nonnegative int avg) {
    double[] v = point.getPoint();
    return String.format("%s1 = %.3f, %s2 = %.3f, h = %.3f mm, e = %.6f", Strings.RHO, v[0], Strings.RHO, v[1], v[2] * 1000, point.getValue() / Math.sqrt(avg));
  }
}