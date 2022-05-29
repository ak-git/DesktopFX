package com.ak.rsm.resistance;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TetrapolarDerivativeResistanceTest {
  @DataProvider(name = "tetrapolar-resistivity")
  public static Object[][] tetrapolarResistivity() {
    return new Object[][] {
        {
            TetrapolarDerivativeResistance.ofSI(1.0, 2.0).dh(Double.NaN).rho(900.1, 1.0),
            "1000 000   2000 000     382 014        900 100              1 000",
            382.014,
            900.1,
            1.0,
            new TetrapolarSystem(1.0, 2.0)
        },
        {
            TetrapolarDerivativeResistance.ofSI(2.0, 1.0).dh(Double.NaN).rho(900.2, -2.0),
            "2000 000   1000 000     382 057        900 200               2 000",
            382.057,
            900.2,
            -2.0,
            new TetrapolarSystem(1.0, 2.0)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(Double.NaN).rho(8.1, -1.0),
            "10 000   30 000     128 916        8 100               1 000",
            128.916,
            8.1,
            -1.0,
            new TetrapolarSystem(0.01, 0.03)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(50.0, 30.0).dh(Double.NaN).rho(8.2, 2.0),
            "50 000   30 000     195 761        8 200              2 000",
            195.761,
            8.2,
            2.0,
            new TetrapolarSystem(0.03, 0.05)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(0.1).rho1(1.0).rho2(1.0).h(Math.random()),
            "10 000   30 000     15 915        1 000              0 000          0 000         0 100",
            15.915,
            1.0,
            0.0,
            new TetrapolarSystem(0.01, 0.03)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(0.1).rho1(10.0).rho2(1.0).h(5.0),
            "10 000   30 000     53 901        3 387              31 312          1 661         0 100",
            53.901,
            3.39,
            31.312,
            new TetrapolarSystem(0.01, 0.03)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(10.0, 20.0).dh(0.1).rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "10 000   20 000     242 751        5 720              13 215          2 804         0 100",
            242.751,
            5.72,
            13.215,
            new TetrapolarSystem(0.01, 0.02)
        },

        {
            TetrapolarDerivativeResistance.ofMilli(30.0, 60.0).dh(0.01).ofOhms(1.0 / Math.PI, 2.0 / Math.PI),
            "30 000   60 000     0 318        0 023              135 000          0 318         0 010",
            0.318,
            9.0 / 400.0,
            9.0 / 400.0 * (60.0 / 0.01),
            new TetrapolarSystem(0.03, 0.06)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(90.0, 30.0).dh(0.01).ofOhms(1.0 / Math.PI, 0.5 / Math.PI),
            "90 000   30 000     0 318        0 060               90 000           0 159         0 010",
            0.318,
            3.0 / 50.0,
            3.0 / 50.0 * (-30.0 / 0.01 / 2.0),
            new TetrapolarSystem(0.09, 0.03)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(40.0, 80.0).dh(0.01).ofOhms(1.0 / Math.PI, 3.0 / Math.PI),
            "40 000   80 000     0 318        0 030              480 000          0 637         0 010",
            0.318,
            3.0 / 100.0,
            3.0 / 100.0 * (80.0 / 0.01 * 2.0),
            new TetrapolarSystem(0.04, 0.08)
        },

        {
            TetrapolarDerivativeResistance.of(new TetrapolarSystem(Metrics.fromMilli(10.0), Metrics.fromMilli(30.0)))
                .dh(Double.NaN).rho(8.1, -8.1),
            "10 000   30 000     128 916        8 100               8 100",
            128.916,
            8.1,
            -8.1,
            new TetrapolarSystem(0.01, 0.03)
        },
    };
  }

  @Test(dataProvider = "tetrapolar-resistivity")
  @ParametersAreNonnullByDefault
  public void test(DerivativeResistance d, String expected,
                   @Nonnegative double ohms, @Nonnegative double resistivity,
                   double derivativeResistivity, TetrapolarSystem system) {
    Assert.assertEquals(d.toString().replaceAll("\\D", " ").strip(), expected, d.toString());
    Assert.assertEquals(d.ohms(), ohms, 0.01, d.toString());
    Assert.assertEquals(d.resistivity(), resistivity, 0.01, d.toString());
    Assert.assertEquals(d.derivativeResistivity(), derivativeResistivity, 0.01, d.toString());
    Assert.assertEquals(d.system(), system, d.toString());
  }

  @DataProvider(name = "tetrapolar-multi-resistivity")
  public static Object[][] tetrapolarMultiResistivity() {
    return new Object[][] {
        {
            TetrapolarDerivativeResistance.milli().dh(Double.NaN).system2(6.0).rho(1.0, 2.0, 3.0, 4.0),
            "6000180002652610003000 30000180007957720004000",
            new double[] {1.0, 2.0},
            new double[] {3.0, 4.0}
        },
        {
            TetrapolarDerivativeResistance.milli().dh(0.2).system2(7.0).rho1(10.0).rho2(1.0).h(5.0),
            "70002100012416054612476053610200 350002100014671043022085267730200",
            new double[] {5.46, 4.30},
            new double[] {24.760, 20.852}
        },
        {
            TetrapolarDerivativeResistance.milli().dh(0.3).system2(8.0).rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "8000240009032745401857546190300 400002400011016536921564958370300",
            new double[] {4.54, 3.69},
            new double[] {18.574, 15.649}
        },
        {
            TetrapolarDerivativeResistance.milli().dh(0.01).system2(10.0)
                .ofOhms(1.0 / Math.PI, 2.0 / Math.PI,
                2.0 / Math.PI, 1.0 / Math.PI),
            "1000030000031800206000003180010 5000030000063700274000003180010",
            new double[] {0.02, 0.027},
            new double[] {60.0, -40.0}
        },
    };
  }

  @Test(dataProvider = "tetrapolar-multi-resistivity")
  @ParametersAreNonnullByDefault
  public void testMulti(Collection<DerivativeResistance> ms, String expected, double[] resistivity, double[] derivativeResistivity) {
    Assert.assertEquals(
        ms.stream().map(
            m -> m.toString().replaceAll("\\D", " ").strip().replaceAll(Strings.SPACE, Strings.EMPTY)
        ).collect(Collectors.joining(Strings.SPACE)), expected, ms.toString());
    Assert.assertEquals(ms.stream().mapToDouble(Resistance::resistivity).toArray(), resistivity, 0.01, ms.toString());
    Assert.assertEquals(ms.stream().mapToDouble(DerivativeResistance::derivativeResistivity).toArray(), derivativeResistivity, 0.01, ms.toString());
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
    DerivativeResistance dR = TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(dHmm).rho1(rho1).rho2(rho2).h(hmm);
    Assert.assertEquals(dR.dOhms(), dRExpected, 0.001, "%s".formatted(dR));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidRhos() {
    TetrapolarDerivativeResistance.milli().dh(0.1).system2(10.0).rho(1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidRhos2() {
    TetrapolarDerivativeResistance.milli().dh(0.01).system2(10.0).rho(1.0, 2.0, 3.0);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testInvalidRhos3() {
    TetrapolarDerivativeResistance.milli().dh(0.01).system2(10.0).rho(1.0, 2.0, 3.0, 4.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidOhms() {
    TetrapolarDerivativeResistance.milli().dh(0.0).system2(10.0).ofOhms(1.0, 2.0, 3.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidOhms2() {
    TetrapolarDerivativeResistance.ofMilli(40.0, 80.0).dh(-0.1).ofOhms(1.0);
  }
}