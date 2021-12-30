package com.ak.rsm.measurement;

import javax.annotation.Nonnegative;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.prediction.TetrapolarDerivativePrediction;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TetrapolarDerivativeMeasurementTest {
  @DataProvider(name = "tetrapolar-measurements")
  public static Object[][] tetrapolarMeasurements() {
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.si(0.1).dh(0.1).system(1.0, 2.0).rho(900.1),
            "1000 000   2000 000      100 0       909          900   270 0              0 000",
            900.1,
            0.0,
            new InexactTetrapolarSystem(0.1, new TetrapolarSystem(1.0, 2.0))
        },
        {
            TetrapolarDerivativeMeasurement.si(0.1).dh(0.2).system(2.0, 1.0).rho(900.2),
            "2000 000   1000 000      100 0       909          900   270 1              0 000",
            900.2,
            0.0,
            new InexactTetrapolarSystem(0.1, new TetrapolarSystem(1.0, 2.0))
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.1).system(10.0, 30.0).rho(8.1),
            "10 000   30 000      0 1       36          8 1   0 16              0 000",
            8.1,
            0.0,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.01, 0.03))
        },
        {
            TetrapolarDerivativeMeasurement.milli(1.0).dh(0.0).system(50.0, 30.0).rho(8.2),
            "50 000   30 000      1 0       28          8   1 1              0 000",
            8.2,
            0.0,
            new InexactTetrapolarSystem(0.001, new TetrapolarSystem(0.03, 0.05))
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.1).system(10.0, 30.0).rho1(1.0).rho2(1.0).h(Math.random()),
            "10 000   30 000      0 1       36          1 00   0 020              0 000",
            1.0,
            0.0,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.01, 0.03))
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.1).system(10.0, 30.0).rho1(10.0).rho2(1.0).h(5.0),
            "10 000   30 000      0 1       36          3 39   0 068              31 312",
            3.39,
            31.312,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.01, 0.03))
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.1).system(10.0, 20.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "10 000   20 000      0 1       20          5 7   0 17              13 875",
            5.72,
            13.875,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.01, 0.02))
        },

        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01).system(30.0, 60.0).ofOhms(1.0 / Math.PI),
            "30 000   60 000      0 1       85          0 0225   0 00023              0 000",
            9.0 / 400.0,
            0.0,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.03, 0.06))
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01).system(90.0, 30.0).ofOhms(1.0 / Math.PI),
            "90 000   30 000      0 1       154          0 0600   0 00040              0 000",
            3.0 / 50.0,
            0.0,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.09, 0.03))
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01).system(40.0, 80.0).ofOhms(1.0 / Math.PI),
            "40 000   80 000      0 1       124          0 0300   0 00023              0 000",
            3.0 / 100.0,
            0.0,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.04, 0.08))
        },
    };
  }

  @Test(dataProvider = "tetrapolar-measurements")
  @ParametersAreNonnullByDefault
  public void test(DerivativeMeasurement d, String expected, @Nonnegative double resistivity,
                   double derivativeResistivity, InexactTetrapolarSystem system) {
    Assert.assertEquals(d.toString().replaceAll("\\D", " ").strip(), expected, d.toString());
    Assert.assertEquals(d.resistivity(), resistivity, 0.01, d.toString());
    Assert.assertEquals(d.derivativeResistivity(), derivativeResistivity, 0.01, d.toString());
    Assert.assertEquals(d.system(), system, d.toString());
    Assert.assertEquals(d.toPrediction(RelativeMediumLayers.SINGLE_LAYER, 1.0),
        TetrapolarDerivativePrediction.of(d.system(), RelativeMediumLayers.SINGLE_LAYER, 1.0,
            new double[] {d.resistivity(), d.derivativeResistivity()}));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testMerge() {
    Measurement m1 = TetrapolarDerivativeMeasurement.milli(0.1).dh(0.1).system(10.0, 30.0).rho(1.0);
    m1.merge(m1);
  }
}