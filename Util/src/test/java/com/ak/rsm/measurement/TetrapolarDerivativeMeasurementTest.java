package com.ak.rsm.measurement;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.prediction.TetrapolarDerivativePrediction;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.resistance.TetrapolarResistance;
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
            TetrapolarDerivativeMeasurement.ofSI(0.1).dh(Double.NaN).system(1.0, 2.0).rho(900.1, 1.0),
            "1000 000   2000 000      100 0       909          900   270 0              1 000",
            900.1,
            1.0,
            new InexactTetrapolarSystem(0.1, new TetrapolarSystem(1.0, 2.0))
        },
        {
            TetrapolarDerivativeMeasurement.ofSI(0.1).dh(Double.NaN).system(2.0, 1.0).rho(900.2, -1.0),
            "2000 000   1000 000      100 0       909          900   270 1               1 000",
            900.2,
            -1.0,
            new InexactTetrapolarSystem(0.1, new TetrapolarSystem(1.0, 2.0))
        },
        {
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(Double.NaN).system(10.0, 30.0).rho(8.1, 2.0),
            "10 000   30 000      0 1       36          8 1   0 16              2 000",
            8.1,
            2.0,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.01, 0.03))
        },
        {
            TetrapolarDerivativeMeasurement.ofMilli(1.0).dh(Double.NaN).system(50.0, 30.0).rho(8.2, -2.0),
            "50 000   30 000      1 0       28          8   1 1               2 000",
            8.2,
            -2.0,
            new InexactTetrapolarSystem(0.001, new TetrapolarSystem(0.03, 0.05))
        },
        {
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(10.0, 30.0)
                .rho1(1.0).rho2(1.0).h(Math.random()),
            "10 000   30 000      0 1       36          1 00   0 020              0 000          0 000         0 100",
            1.0,
            0.0,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.01, 0.03))
        },
        {
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(10.0, 30.0)
                .rho1(10.0).rho2(1.0).h(5.0),
            "10 000   30 000      0 1       36          3 39   0 068              31 312          1 661         0 100",
            3.39,
            31.312,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.01, 0.03))
        },
        {
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(10.0, 20.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "10 000   20 000      0 1       20          5 7   0 17              13 215          2 804         0 100",
            5.72,
            13.215,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.01, 0.02))
        },

        {
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.01).system(30.0, 60.0)
                .ofOhms(1.0 / Math.PI, 2.0 / Math.PI),
            "30 000   60 000      0 1       85          0 0225   0 00023              135 000          0 318         0 010",
            9.0 / 400.0,
            9.0 / 400.0 * (60.0 / 0.01),
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.03, 0.06))
        },
        {
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.01).system(90.0, 30.0)
                .ofOhms(1.0 / Math.PI, 0.5 / Math.PI),
            "90 000   30 000      0 1       154          0 0600   0 00040               90 000           0 159         0 010",
            3.0 / 50.0,
            3.0 / 50.0 * (-30.0 / 0.01 / 2.0),
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.09, 0.03))
        },
        {
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.01).system(40.0, 80.0)
                .ofOhms(1.0 / Math.PI, 3.0 / Math.PI),
            "40 000   80 000      0 1       124          0 0300   0 00023              480 000          0 637         0 010",
            3.0 / 100.0,
            3.0 / 100.0 * (80.0 / 0.01 * 2.0),
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
    Assert.assertEquals(d.inexact(), system, d.toString());
    Assert.assertEquals(d.toPrediction(RelativeMediumLayers.SINGLE_LAYER, 1.0),
        TetrapolarDerivativePrediction.of(d, RelativeMediumLayers.SINGLE_LAYER, 1.0));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testMerge() {
    Measurement m1 = TetrapolarDerivativeMeasurement.ofSI(0.1).dh(Double.NaN).system(10.0, 30.0).rho(1.0, 2.0);
    m1.merge(m1);
  }

  @DataProvider(name = "tetrapolar-multi-measurements")
  public static Object[][] tetrapolarMultiMeasurements() {
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(Double.NaN).system2(6.0).rho(1.0, 2.0, 3.0, 4.0),
            "600018000011810000333000 3000018000013120000444000",
            new double[] {1.0, 2.0},
            new double[] {3.0, 4.0}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.2).system2(7.0).rho1(10.0).rho2(1.0).h(5.0),
            "7000210000122550162476053610200 3500021000013843000822085267730200",
            new double[] {5.46, 4.30},
            new double[] {24.760, 20.852}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.3).system2(8.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "8000240000126450111857546190300 4000024000014536200601564958370300",
            new double[] {4.45, 3.62},
            new double[] {18.575, 15.649}
        },

        {
            TetrapolarDerivativeMeasurement.milli(-0.1).dh(0.01).withShiftError().system2(8.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "7900241000127440111957801580010 3990024100014536600611618202040010",
            new double[] {4.42, 3.65},
            new double[] {19.578, 16.182}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01).withShiftError().system2(8.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "8100239000126450111934801650010 4010023900014635800591598001960010",
            new double[] {4.49, 3.58},
            new double[] {19.348, 15.980}
        },

        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01).system2(10.0)
                .ofOhms(1.0 / Math.PI, 2.0 / Math.PI,
                2.0 / Math.PI, 1.0 / Math.PI),
            "10000300000136002000000406000003180010 50000300000161002670000364000003180010",
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
    TetrapolarDerivativeMeasurement.ofSI(0.01).dh(0.1).system(10, 20.0)
        .ofOhms(DoubleStream.generate(Math::random).limit(Math.random() > 0.5 ? 1 : 3).toArray());
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testInvalidRhos() {
    TetrapolarDerivativeMeasurement.ofSI(0.01).dh(0.1).system(10, 20.0).rho(1.0, 2.0, 3.0, 4.0);
  }

  @DataProvider(name = "derivative-measurements")
  public static Object[][] derivativeMeasurements() {
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(9.0).rho2(1.0).h(5.0),
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(9.0).rho2(1.0).rho3(1.0).hStep(0.1).p(50, 50),
        },
        {
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(9.0).rho2(1.0).h(5.0),
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(9.0).rho2(1.0).rho3(1.0).hStep(0.01).p(500, 500),
        },
        {
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(1.0).rho2(9.0).h(10.0),
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(1.0).rho2(1.0).rho3(9.0).hStep(0.1).p(50, 50),
        },
        {
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(1.0).rho2(9.0).h(10.0),
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(1.0).rho2(1.0).rho3(9.0).hStep(0.01).p(500, 500),
        },
    };
  }

  @Test(dataProvider = "derivative-measurements")
  public void testDerivativeMeasurements(@Nonnull DerivativeResistivity dm1, @Nonnull DerivativeResistivity dm2) {
    Assert.assertEquals(dm1.resistivity(), dm2.resistivity(), 0.1);
    Assert.assertEquals(dm1.derivativeResistivity(), dm2.derivativeResistivity(), 0.1);
  }

  @DataProvider(name = "d-resistivity")
  public static Object[][] dResistivity() {
    return new Object[][] {
        {0.7, Double.POSITIVE_INFINITY, 10.0, 0.1},
        {0.7, Double.POSITIVE_INFINITY, 20.0, -0.1},
        {1.0, 1.0, 30.0, -0.1},
    };
  }

  @Test(dataProvider = "d-resistivity")
  public void testDResistivity(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double hmm, double dHmm) {
    double dRExpected = TetrapolarResistance.ofMilli(10.0, 30.0).rho1(rho1).rho2(rho2).h(hmm + dHmm).ohms() -
        TetrapolarResistance.ofMilli(10.0, 30.0).rho1(rho1).rho2(rho2).h(hmm).ohms();
    var dR = TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(dHmm).system(10.0, 30.0)
        .rho1(rho1).rho2(rho2).h(hmm);
    Assert.assertEquals(dR.dOhms(), dRExpected, 0.001, "%s".formatted(dR));
  }
}