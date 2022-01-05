package com.ak.rsm.measurement;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.prediction.TetrapolarDerivativePrediction;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Strings;
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
            TetrapolarDerivativeMeasurement.si(0.1).dh(-0.1).system(10.0, 30.0).rho(8.1),
            "10000 000   30000 000      100 0       35589          8 1   0 16              0 000",
            8.1,
            0.0,
            new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 30.0))
        },
        {
            TetrapolarDerivativeMeasurement.si(1.0).dh(0.0).system(50.0, 30.0).rho(8.2),
            "50000 000   30000 000      1000 0       28247          8   1 1              0 000",
            8.2,
            0.0,
            new InexactTetrapolarSystem(1.0, new TetrapolarSystem(30.0, 50.0))
        },
        {
            TetrapolarDerivativeMeasurement.si(0.1).dh(0.1).system(10.0, 30.0).rho1(1.0).rho2(1.0).h(Math.random()),
            "10000 000   30000 000      100 0       35589          1 00   0 020              0 000",
            1.0,
            0.0,
            new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 30.0))
        },
        {
            TetrapolarDerivativeMeasurement.si(0.1).dh(0.1).system(10.0, 30.0).rho1(10.0).rho2(1.0).h(5.0),
            "10000 000   30000 000      100 0       35589          3 39   0 068              31 312",
            3.39,
            31.312,
            new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 30.0))
        },
        {
            TetrapolarDerivativeMeasurement.si(0.1).dh(0.1).system(10.0, 20.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "10000 000   20000 000      100 0       19586          5 7   0 17              13 875",
            5.72,
            13.875,
            new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 20.0))
        },

        {
            TetrapolarDerivativeMeasurement.si(0.1).dh(0.01).system(30.0, 60.0).ofOhms(1.0 / Math.PI, 2.0 / Math.PI),
            "30000 000   60000 000      100 0       84742          22 5   0 23              135000 000",
            9.0 / 0.40,
            9.0 / 0.40 * (60.0 / 0.01),
            new InexactTetrapolarSystem(0.1, new TetrapolarSystem(30.0, 60.0))
        },
        {
            TetrapolarDerivativeMeasurement.si(0.1).dh(0.01).system(90.0, 30.0).ofOhms(1.0 / Math.PI, 0.5 / Math.PI),
            "90000 000   30000 000      100 0       153986          60 0   0 40               90000 000",
            3.0 / 0.05,
            3.0 / 0.05 * (-30.0 / 0.01 / 2.0),
            new InexactTetrapolarSystem(0.1, new TetrapolarSystem(90.0, 30.0))
        },
        {
            TetrapolarDerivativeMeasurement.si(0.1).dh(0.01).system(40.0, 80.0).ofOhms(1.0 / Math.PI, 3.0 / Math.PI),
            "40000 000   80000 000      100 0       124360          30 0   0 22              480000 000",
            3.0 / 0.1,
            3.0 / 0.1 * (80.0 / 0.01 * 2.0),
            new InexactTetrapolarSystem(0.1, new TetrapolarSystem(40.0, 80.0))
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
    Assert.assertEquals(d.inexact(), system, d.toString());
    Assert.assertEquals(d.toPrediction(RelativeMediumLayers.SINGLE_LAYER, 1.0),
        TetrapolarDerivativePrediction.of(d.inexact(), RelativeMediumLayers.SINGLE_LAYER, 1.0,
            new double[] {d.resistivity(), d.derivativeResistivity()}));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testMerge() {
    Measurement m1 = TetrapolarDerivativeMeasurement.si(0.1).dh(0.1).system(10.0, 30.0).rho(1.0);
    m1.merge(m1);
  }

  @DataProvider(name = "tetrapolar-multi-measurements")
  public static Object[][] tetrapolarMultiMeasurements() {
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.1).system2(6.0).rho(1.0),
            "600018000011810000330000 3000018000013110000220000",
            new double[] {1.0, 1.0},
            new double[] {0.0, 0.0}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.2).system2(7.0).rho1(10.0).rho2(1.0).h(5.0),
            "70002100001225501624760 35000210000138430008220852",
            new double[] {5.46, 4.30},
            new double[] {24.760, 20.852}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.3).system2(8.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "80002400001264501120189 40000240000145362006017296",
            new double[] {4.45, 3.62},
            new double[] {20.189, 17.296}
        },

        {
            TetrapolarDerivativeMeasurement.milli(-0.1).dh(0.01).withShiftError().system2(8.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "79002410001274401120961 39900241000145366006117729",
            new double[] {4.42, 3.65},
            new double[] {20.961, 17.729}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01).withShiftError().system2(8.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "81002390001264501120689 40100239000146358005917541",
            new double[] {4.49, 3.58},
            new double[] {20.689, 17.541}
        },

        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01).system2(10.0)
                .ofOhms(1.0 / Math.PI, 2.0 / Math.PI,
                2.0 / Math.PI, 1.0 / Math.PI),
            "100003000001360020000004060000 500003000001610026700003640000",
            new double[] {0.02, 0.027},
            new double[] {60.0, -40.0}
        },
    };
  }

  @Test(dataProvider = "tetrapolar-multi-measurements")
  @ParametersAreNonnullByDefault
  public void testMulti(Collection<DerivativeMeasurement> ms, String expected, double[] resistivity, double[] derivativeResistivity) {
    Assert.assertEquals(
        ms.stream().map(
            m -> m.toString().replaceAll("\\D", " ").strip().replaceAll(Strings.SPACE, Strings.EMPTY)
        ).collect(Collectors.joining(Strings.SPACE)), expected, ms.toString());
    Assert.assertEquals(ms.stream().mapToDouble(Measurement::resistivity).toArray(), resistivity, 0.01, ms.toString());
    Assert.assertEquals(ms.stream().mapToDouble(DerivativeMeasurement::derivativeResistivity).toArray(), derivativeResistivity, 0.01, ms.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class, invocationCount = 3)
  public void testInvalidOhms() {
    TetrapolarDerivativeMeasurement.si(0.01).dh(0.1).system(10, 20.0)
        .ofOhms(DoubleStream.generate(Math::random).limit(Math.random() > 0.5 ? 1 : 3).toArray());
  }
}