package com.ak.rsm;

import java.util.Arrays;
import java.util.Random;

import javax.annotation.Nonnegative;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class InverseTest {
  @DataProvider(name = "layer1")
  public static Object[][] layer1() {
    TetrapolarSystem[] systems2 = LayersProvider.systems2_10mm();
    Random random = new Random();
    int rho = random.nextInt(9) + 1;
    return new Object[][] {
        {
            systems2,
            Arrays.stream(LayersProvider.rangeSystems(systems2, LayersProvider.layer1(rho)))
                .map(r -> r + random.nextGaussian()).toArray(),
            rho
        },
    };
  }

  @Test(dataProvider = "layer1")
  @ParametersAreNonnullByDefault
  public void testInverseLayer1(TetrapolarSystem[] systems, double[] rOhms, @Nonnegative double expected) {
    MediumLayers medium = Inverse.inverseStatic(TetrapolarMeasurement.of(systems, rOhms));
    Assert.assertEquals(medium.rho(), expected, 0.2, medium.toString());
  }

  @DataProvider(name = "layer2")
  public static Object[][] layer2() {
    TetrapolarSystem[] systems2 = LayersProvider.systems2_10mm();
    TetrapolarSystem[] systems4 = LayersProvider.systems4(10);
    return new Object[][] {
        {
            systems2,
            Arrays.stream(LayersProvider.rangeSystems(systems2, LayersProvider.layer1(10.0))).toArray(),
            new double[] {10.0, 10.0, Double.NaN}
        },
        {
            systems4,
            Arrays.stream(LayersProvider.rangeSystems(systems4,
                LayersProvider.layer2(10.0, 1.0, Metrics.fromMilli(10.0)))
            ).toArray(),
            new double[] {10.0, 1.0, Metrics.fromMilli(10.0)}
        },
        {
            systems4,
            Arrays.stream(LayersProvider.rangeSystems(systems4,
                LayersProvider.layer2(1.0, 10.0, Metrics.fromMilli(5.0)))
            ).toArray(),
            new double[] {1.0, 10.0, Metrics.fromMilli(5.0)}
        },
    };
  }

  @Test(dataProvider = "layer2")
  @ParametersAreNonnullByDefault
  public void testInverseLayer2(TetrapolarSystem[] systems, double[] rOhms, double[] expected) {
    Random random = new Random();
    MediumLayers medium = Inverse.inverseStatic(
        TetrapolarMeasurement.of(systems,
            Arrays.stream(rOhms).map(x -> x + random.nextGaussian() / x / 10.0).toArray()));
    Assert.assertEquals(medium.rho1(), expected[0], 0.1, medium.toString());
    Assert.assertEquals(medium.rho2(), expected[1], 0.1, medium.toString());
    Assert.assertEquals(medium.h(), expected[2], 0.1, medium.toString());
  }

  @DataProvider(name = "theoryDynamicParameters2")
  public static Object[][] theoryDynamicParameters2() {
    TetrapolarSystem[] systems1 = {
        new TetrapolarSystem(10.0, 20.0, MILLI(METRE))
    };
    TetrapolarSystem[] systems2 = LayersProvider.systems2_10mm();
    TetrapolarSystem[] systems4 = LayersProvider.systems4(7.0);
    double dh = Metrics.fromMilli(-0.001);
    double h = Metrics.fromMilli(5.0);
    return new Object[][] {
        {
            systems1,
            LayersProvider.rangeSystems(systems1, LayersProvider.layer2(1.0, 9.0, h)),
            LayersProvider.rangeSystems(systems1, LayersProvider.layer2(1.0, 9.0, h + dh)),
            dh,
            new double[] {new NormalizedApparent2Rho(systems1[0]).value(0.8, h), 0.0, Double.NaN}
        },
        {
            systems2,
            LayersProvider.rangeSystems(systems2, LayersProvider.layer2(1.0, Double.POSITIVE_INFINITY, h)),
            LayersProvider.rangeSystems(systems2, LayersProvider.layer2(1.0, Double.POSITIVE_INFINITY, h + dh)),
            dh,
            new double[] {1.0, 1.0, h}
        },
        {
            systems4,
            LayersProvider.rangeSystems(systems4, LayersProvider.layer2(10.0, 0.0, h)),
            LayersProvider.rangeSystems(systems4, LayersProvider.layer2(10.0, 0.0, h + dh)),
            dh,
            new double[] {10.0, -1.0, h}
        },
    };
  }

  @Test(dataProvider = "theoryDynamicParameters2")
  @ParametersAreNonnullByDefault
  public void testInverseDynamicLayer2(TetrapolarSystem[] systems, double[] rOhms, double[] rOhmsAfter, double dh, double[] expected) {
    MediumLayers medium = Inverse.inverseDynamic(TetrapolarDerivativeMeasurement.of(systems, rOhms, rOhmsAfter, dh));
    Assert.assertEquals(medium.rho1(), expected[0], 0.1, medium.toString());
    Assert.assertEquals(medium.k12(), expected[1], 0.1, medium.toString());
    Assert.assertEquals(medium.h(), expected[2], 0.1, medium.toString());
  }

  @DataProvider(name = "waterDynamicParameters2")
  public static Object[][] waterDynamicParameters2() {
    double dh = -Metrics.fromMilli(10.0 / 200.0);
    TetrapolarSystem[] systems = LayersProvider.systems2_10mm();
    return new Object[][] {
        // h = 5 mm, rho1 = 0.7, rho2 = Inf
        {
            systems,
            new double[] {30.971, 61.860},
            new double[] {31.278, 62.479},
            dh
        },
        // h = 10 mm, rho1 = 0.7, rho2 = Inf
        {
            systems,
            new double[] {16.761, 32.246},
            new double[] {16.821, 32.383},
            dh
        },
        // h = 15 mm, rho1 = 0.7, rho2 = Inf
        {
            systems,
            new double[] {13.338, 23.903},
            new double[] {13.357, 23.953},
            dh
        },
        // h = 20 mm, rho1 = 0.7, rho2 = Inf
        {
            systems,
            new double[] {12.187, 20.567},
            new double[] {12.194, 20.589},
            dh
        },
        // h = 25 mm, rho1 = 0.7, rho2 = Inf
        {
            systems,
            new double[] {11.710, 18.986},
            new double[] {11.714, 18.998},
            dh
        },
        // h = 30 mm, rho1 = 0.7, rho2 = Inf
        {
            systems,
            new double[] {11.482, 18.152},
            new double[] {11.484, 18.158},
            dh
        },
        // h = 35 mm, rho1 = 0.7, rho2 = Inf
        {
            systems,
            new double[] {11.361, 17.674},
            new double[] {11.362, 17.678},
            dh
        },
    };
  }

  @Test(dataProvider = "waterDynamicParameters2")
  @ParametersAreNonnullByDefault
  public void testInverseDynamicLayer2(TetrapolarSystem[] systems, double[] rOhms, double[] rOhmsAfter, double dh) {
    MediumLayers medium = Inverse.inverseDynamic(TetrapolarDerivativeMeasurement.of(systems, rOhms, rOhmsAfter, dh));
    System.out.println(medium);
  }
}