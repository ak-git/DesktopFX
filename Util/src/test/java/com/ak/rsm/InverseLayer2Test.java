package com.ak.rsm;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.rsm.InexactTetrapolarSystem.systems2;
import static com.ak.rsm.InexactTetrapolarSystem.systems4;

public class InverseLayer2Test {
  private static final Logger LOGGER = Logger.getLogger(InverseLayer2Test.class.getName());

  @DataProvider(name = "layer2")
  public static Object[][] layer2() {
    InexactTetrapolarSystem[] systems2 = systems2(0.1, 10.0);
    InexactTetrapolarSystem[] systems4 = systems4(0.1, 10.0);
    return new Object[][] {
        {
            systems2,
            Arrays.stream(systems2)
                .mapToDouble(s -> new Resistance1Layer(s.toExact()).value(10.0)).toArray(),
            new double[] {10.0, 10.0, Double.NaN}
        },
        {
            systems4,
            Arrays.stream(systems4)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(10.0, 1.0, Metrics.fromMilli(10.0))).toArray(),
            new double[] {10.0, 1.0, Metrics.fromMilli(10.0)}
        },
        {
            systems4,
            Arrays.stream(systems4)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(1.0, 10.0, Metrics.fromMilli(5.0))).toArray(),
            new double[] {1.0, 10.0, Metrics.fromMilli(5.0)}
        },
    };
  }

  @Test(dataProvider = "layer2")
  @ParametersAreNonnullByDefault
  public void testInverseLayer2(InexactTetrapolarSystem[] systems, double[] rOhms, double[] expected) {
    Random random = new Random();
    MediumLayers medium = Inverse.inverseStatic(
        TetrapolarMeasurement.of(systems,
            Arrays.stream(rOhms).map(x -> x + random.nextGaussian() / x / 10.0).toArray()
        )
    );
    Assert.assertEquals(medium.rho1(), expected[0], 0.1, medium.toString());
    Assert.assertEquals(medium.rho2(), expected[1], 0.1, medium.toString());
    Assert.assertEquals(medium.h(), expected[2], 0.1, medium.toString());
    LOGGER.info(medium::toString);
  }

  @DataProvider(name = "theoryDynamicParameters2")
  public static Object[][] theoryDynamicParameters2() {
    InexactTetrapolarSystem[] systems1 = {
        InexactTetrapolarSystem.milli(0.1).s(10.0).l(20.0)
    };
    InexactTetrapolarSystem[] systems2 = systems2(0.1, 10.0);
    InexactTetrapolarSystem[] systems4 = systems4(0.1, 7.0);
    double dh = Metrics.fromMilli(-0.001);
    double h = Metrics.fromMilli(5.0);
    return new Object[][] {
        {
            systems1,
            Arrays.stream(systems1)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(1.0, 9.0, h)).toArray(),
            Arrays.stream(systems1)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(1.0, 9.0, h + dh)).toArray(),
            dh,
            new double[] {new NormalizedApparent2Rho(systems1[0].toExact().toRelative()).value(0.8, h / systems1[0].toExact().getL()), 0.0, Double.NaN}
        },
        {
            systems2,
            Arrays.stream(systems2)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(1.0, Double.POSITIVE_INFINITY, h)).toArray(),
            Arrays.stream(systems2)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(1.0, Double.POSITIVE_INFINITY, h + dh)).toArray(),
            dh,
            new double[] {1.0, 1.0, h}
        },
        {
            systems4,
            Arrays.stream(systems4)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(10.0, 0.0, h)).toArray(),
            Arrays.stream(systems4)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(10.0, 0.0, h + dh)).toArray(),
            dh,
            new double[] {10.0, -1.0, h}
        },
    };
  }

  @DataProvider(name = "dynamicParameters2")
  public static Object[][] dynamicParameters2() {
    return new Object[][] {
        {
            systems2(0.1, 7.0),
            new double[] {113.341, 167.385},
            new double[] {113.341 + 0.091, 167.385 + 0.273},
            Metrics.fromMilli(0.15),
            new double[] {5.211, -0.534, Metrics.fromMilli(15.28)}
        },
        {
            systems2(0.1, 8.0),
            new double[] {93.4, 162.65},
            new double[] {93.5, 162.85},
            Metrics.fromMilli(0.12),
            new double[] {5.302, -0.094, Metrics.fromMilli(7.89)}
        },
        {
            systems2(0.1, 7.0),
            new double[] {136.5, 207.05},
            new double[] {136.65, 207.4},
            Metrics.fromMilli(0.15),
            new double[] {6.332, -0.205, Metrics.fromMilli(10.43)}
        },
    };
  }

  @DataProvider(name = "waterDynamicParameters2-E5731")
  public static Object[][] waterDynamicParameters2() {
    double dh = -Metrics.fromMilli(10.0 / 200.0);
    return new Object[][] {
        // h = 5 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {29.47, 65.68},
            new double[] {29.75, 66.35},
            dh,
            new double[] {0.694, 1.0, Metrics.fromMilli(4.96)}
        },
        // h = 10 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {16.761, 32.246},
            new double[] {16.821, 32.383},
            dh,
            new double[] {0.699, 1.0, Metrics.fromMilli(9.98)}
        },
        // h = 15 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {13.338, 23.903},
            new double[] {13.357, 23.953},
            dh,
            new double[] {0.698, 1.0, Metrics.fromMilli(14.48)}
        },
        // h = 20 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {12.187, 20.567},
            new double[] {12.194, 20.589},
            dh,
            new double[] {0.7, 1.0, Metrics.fromMilli(19.95)}
        },
        // h = 25 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {11.710, 18.986},
            new double[] {11.714, 18.998},
            dh,
            new double[] {0.7, 1.0, Metrics.fromMilli(25.0)}
        },
        // h = 30 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {11.482, 18.152},
            new double[] {11.484, 18.158},
            dh,
            new double[] {0.7, 1.0, Metrics.fromMilli(30.0)}
        },
        // h = 35 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {11.361, 17.674},
            new double[] {11.362, 17.678},
            dh,
            new double[] {0.698, 1.0, Metrics.fromMilli(34.06)}
        },
    };
  }

  @DataProvider(name = "allDynamicParameters2")
  public static Object[][] allDynamicParameters2() {
    return Stream.concat(Arrays.stream(theoryDynamicParameters2()), Arrays.stream(dynamicParameters2())).toArray(Object[][]::new);
  }

  @Test(dataProvider = "allDynamicParameters2")
  @ParametersAreNonnullByDefault
  public void testInverseDynamicLayer2(InexactTetrapolarSystem[] systems, double[] rOhms, double[] rOhmsAfter, double dh, double[] expected) {
    MediumLayers medium = Inverse.inverseDynamic(TetrapolarDerivativeMeasurement.of(systems, rOhms, rOhmsAfter, dh));
    Assert.assertEquals(medium.rho1(), expected[0], 0.1, medium.toString());
    Assert.assertEquals(medium.k12(), expected[1], 0.1, medium.toString());
    Assert.assertEquals(Metrics.toMilli(medium.h()), Metrics.toMilli(expected[2]), 0.01, medium.toString());
    LOGGER.info(medium::toString);
  }
}