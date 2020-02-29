package com.ak.rsm;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.rsm.LayersProvider.layer1;
import static com.ak.rsm.LayersProvider.layer2;
import static com.ak.rsm.LayersProvider.rOhms;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class Resistance2LayerTest {
  private static final double[] EMPTY_DOUBLES = {};

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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIllegal() {
    new Resistance2Layer(new TetrapolarSystem(10, 20, MILLI(METRE))).value(new double[] {1});
  }

  @DataProvider(name = "layer2Static")
  public static Object[][] layer2Static() {
    TetrapolarSystem[] systems1 = {new TetrapolarSystem(10.0, 30.0, MILLI(METRE))};
    Random random = new Random();
    return new Object[][] {
        {
            systems1,
            Arrays.stream(rOhms(systems1, layer1(1.0))).map(r -> r + random.nextGaussian()).toArray(),
            EMPTY_DOUBLES
        },
    };
  }

  @Test(dataProvider = "layer2Static", enabled = false)
  @ParametersAreNonnullByDefault
  public void testInverseStatic(TetrapolarSystem[] systems, double[] rOhms, double[] expectedH) {
    Assert.assertEquals(Resistance2Layer.inverseStaticLinear(systems, rOhms).getH(), expectedH, Metrics.fromMilli(3));
    Assert.assertEquals(Resistance2Layer.inverseStaticLog(systems, rOhms).getH(), expectedH, Metrics.fromMilli(3));
    Assert.assertEquals(Resistance2Layer.inverseDynamic(systems, rOhms, rOhms, -Metrics.fromMilli(0.1)).getH(), expectedH, Metrics.fromMilli(3));
  }

  @Test(dataProviderClass = LayersProvider.class, dataProvider = "waterDynamicParameters2E6275", enabled = false)
  @ParametersAreNonnullByDefault
  public void testInverseE6275(TetrapolarSystem[] systems, double[] rOhmsBefore, double[] rOhmsAfter, double dh) {
    Resistance2Layer.inverseDynamic(systems, rOhmsBefore, rOhmsAfter, dh);
  }

  @DataProvider(name = "layer2Dynamic")
  public static Object[][] layer2Dynamic() {
    TetrapolarSystem[] systems2 = LayersProvider.systems2_10mm();
    TetrapolarSystem[] systems4 = LayersProvider.systems4(10);
    double dh = -0.1;
    return new Object[][] {
        {
            systems2,
            rOhms(systems2, layer1(2.0)),
            rOhms(systems2, layer1(2.0)),
            Metrics.fromMilli(dh),
            EMPTY_DOUBLES
        },
        {
            systems4,
            rOhms(systems4, layer2(9.0, 1.0, 5.0)),
            rOhms(systems4, layer2(9.0, 1.0, 5.0 + dh)),
            Metrics.fromMilli(dh),
            new double[] {Metrics.fromMilli(5.0)}
        },
        {
            systems4,
            rOhms(systems4, layer2(1.0, 4.0, 3.0)),
            rOhms(systems4, layer2(1.0, 4.0, 3.0 + dh)),
            Metrics.fromMilli(dh),
            new double[] {Metrics.fromMilli(3.0)}
        },
        {
            systems2,
            rOhms(systems2, layer2(0.7, Double.POSITIVE_INFINITY, 4.0)),
            rOhms(systems2, layer2(0.7, Double.POSITIVE_INFINITY, 4.0 + dh)),
            Metrics.fromMilli(dh),
            new double[] {Metrics.fromMilli(4.0)}
        },
    };
  }

  @Test(dataProvider = "layer2Dynamic", enabled = false)
  @ParametersAreNonnullByDefault
  public void testInverse(TetrapolarSystem[] systems, double[] rOhmsBefore, double[] rOhmsAfter, double dh, double[] expectedH) {
    Random random = new Random();
    double[] noise = IntStream.range(0, systems.length).mapToDouble(value -> random.nextGaussian() / 10).toArray();
    for (int i = 0; i < noise.length; i++) {
      rOhmsBefore[i] += noise[i];
      rOhmsAfter[i] += noise[i];
    }
    Logger logger = Logger.getLogger(Resistance2LayerTest.class.getName());
    logger.info(() -> String.format("2 Layers - inverseStaticLinear%n%s", Resistance2Layer.inverseStaticLinear(systems, rOhmsBefore)));
    logger.info(() -> String.format("2 Layers - inverseStaticLog%n%s", Resistance2Layer.inverseStaticLog(systems, rOhmsBefore)));
    Medium medium = Resistance2Layer.inverseDynamic(systems, rOhmsBefore, rOhmsAfter, dh);
    logger.warning(() -> String.format("2 Layers - inverseDynamic%n%s", medium));
    Assert.assertEquals(medium.getH(), expectedH, Metrics.fromMilli(2));
  }
}