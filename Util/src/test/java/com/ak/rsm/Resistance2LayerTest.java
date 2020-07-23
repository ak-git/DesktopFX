package com.ak.rsm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.IntToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.util.LineFileBuilder;
import com.ak.util.Metrics;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.rsm.LayersProvider.layer2;
import static java.lang.StrictMath.log;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class Resistance2LayerTest {
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
  public void testLayer(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    Assert.assertEquals(new Resistance2Layer(system).value(rho[0], rho[1], Metrics.fromMilli(hmm)), rOhm, 0.001);
  }

  private static double[] calculate(@Nonnegative double relativeError, @Nonnull double[] sToL, double k, @Nonnegative double hToL) {
    final double L = 1.0;

    TetrapolarSystem[] systems = {
        new TetrapolarSystem(L * sToL[0], L * sToL[1], METRE),
        new TetrapolarSystem(L * sToL[1], L, METRE),
    };
    double[] rho = {1.0, 1.0 / Layers.getRho1ToRho2(k)};
    return IntStream.of(2, 5).mapToObj(n -> {
      int signS1 = (n & 1) == 0 ? 1 : -1;
      int signL = (n & 2) == 0 ? 1 : -1;
      int signS2 = (n & 4) == 0 ? 1 : -1;

      TetrapolarSystem[] systemsError = {
          systems[0].newWithError(relativeError, signS1, signL),
          systems[1].newWithError(relativeError, signL, signS2)
      };

      double[] rOhms = LayersProvider.rangeSystems(systemsError, layer2(rho[0], rho[1], hToL));
      double[] rDiff = LayersProvider.rangeSystems(systems, s -> new DerivativeApparent2Rho(s).value(k, hToL));

      double[] logApparent = rangeSystems(systems.length,
          index -> log(systems[index].getApparent(rOhms[index])));
      double[] logDiff = rangeSystems(systems.length,
          index -> log(Math.abs(rDiff[index])));

      double[] measured = rangeSystems(systems.length, index -> logApparent[index] - logDiff[index]);

      PointValuePair optimize = Simplex.optimizeCMAES(hk -> {
        double[] logApparentPredicted = rangeSystems(systems.length,
            index -> new Log1pApparent2Rho(systems[index]).value(hk[1], hk[0])
        );
        double[] diffPredicted = rangeSystems(systems.length, index -> new DerivativeApparent2Rho(systems[index]).value(hk[1], hk[0]));
        double[] predicted = rangeSystems(systems.length, index -> logApparentPredicted[index] - log(Math.abs(diffPredicted[index])));
        return Inequality.absolute().applyAsDouble(measured, predicted);
      }, new SimpleBounds(new double[] {0.0, -1.0}, new double[] {hToL, 1.0}), new double[] {hToL, k}, new double[] {0.001, 0.001});

      double eH = Inequality.absolute().applyAsDouble(optimize.getPoint()[0], hToL) / relativeError;
      double eK = Inequality.absolute().applyAsDouble(optimize.getPoint()[1], k) / relativeError;
      Logger.getAnonymousLogger().info(() ->
          String.format("%d [%+d %+d %+d]\th / L = %.4f; eH = %.15f; eK = %.15f; [%s]",
              n, signS1, signL, signS2, hToL, eH, eK,
              Arrays.toString(sToL))
      );
      return new double[] {eH, eK};
    }).parallel().collect(Collectors.teeing(
        Collectors.maxBy(Comparator.comparingDouble(value -> value[0])),
        Collectors.maxBy(Comparator.comparingDouble(value -> value[1])),
        (doubles, doubles2) -> Stream.of(doubles, doubles2)
            .map(d -> d.orElse(new double[] {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY}))
            .reduce((d1, d2) -> IntStream.range(0, d1.length).mapToDouble(i -> Math.max(d1[i], d2[i])).toArray())
    )).orElseThrow();
  }

  @Test(enabled = false)
  public void testInverseErrorByH() {
    LineFileBuilder.<double[]>of("%.3f %.3f %.15f")
        .xStream(() -> DoubleStream.of(-1, -0.1, 0.1, 1.0))
        .yLog10Range(0.005, 0.5)
        .add("h.txt", value -> value[0])
        .add("k.txt", value -> value[1])
        .generate((k, hToL) -> calculate(1.0e-6,
            new double[] {0.2, 0.6}, k, hToL)
        );
  }

  private static double[] rangeSystems(@Nonnegative int length, @Nonnull IntToDoubleFunction mapper) {
    return IntStream.range(0, length).mapToDouble(mapper).toArray();
  }
}