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
            TetrapolarDerivativeResistance.ofSI(1.0, 2.0).dh(0.1).rho(900.1),
            "1000 000   2000 000     382 014        900 100              0 000",
            382.014,
            900.1,
            0.0,
            new TetrapolarSystem(1.0, 2.0)
        },
        {
            TetrapolarDerivativeResistance.ofSI(2.0, 1.0).dh(0.2).rho(900.2),
            "2000 000   1000 000     382 057        900 200              0 000",
            382.057,
            900.2,
            0.0,
            new TetrapolarSystem(1.0, 2.0)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(-0.1).rho(8.1),
            "10 000   30 000     128 916        8 100              0 000",
            128.916,
            8.1,
            0.0,
            new TetrapolarSystem(0.01, 0.03)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(50.0, 30.0).dh(0.0).rho(8.2),
            "50 000   30 000     195 761        8 200              0 000",
            195.761,
            8.2,
            0.0,
            new TetrapolarSystem(0.03, 0.05)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(0.1).rho1(1.0).rho2(1.0).h(Math.random()),
            "10 000   30 000     15 915        1 000              0 000",
            15.915,
            1.0,
            0.0,
            new TetrapolarSystem(0.01, 0.03)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(0.1).rho1(10.0).rho2(1.0).h(5.0),
            "10 000   30 000     53 901        3 387              31 312",
            53.901,
            3.39,
            31.312,
            new TetrapolarSystem(0.01, 0.03)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(10.0, 20.0).dh(0.1).rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "10 000   20 000     242 751        5 720              13 875",
            242.751,
            5.72,
            13.875,
            new TetrapolarSystem(0.01, 0.02)
        },

        {
            TetrapolarDerivativeResistance.ofMilli(30.0, 60.0).dh(0.01).ofOhms(1.0 / Math.PI, 2.0 / Math.PI),
            "30 000   60 000     0 318        0 023              135 000",
            0.318,
            9.0 / 400.0,
            9.0 / 400.0 * (60.0 / 0.01),
            new TetrapolarSystem(0.03, 0.06)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(90.0, 30.0).dh(0.01).ofOhms(1.0 / Math.PI, 0.5 / Math.PI),
            "90 000   30 000     0 318        0 060               90 000",
            0.318,
            3.0 / 50.0,
            3.0 / 50.0 * (-30.0 / 0.01 / 2.0),
            new TetrapolarSystem(0.09, 0.03)
        },
        {
            TetrapolarDerivativeResistance.ofMilli(40.0, 80.0).dh(0.01).ofOhms(1.0 / Math.PI, 3.0 / Math.PI),
            "40 000   80 000     0 318        0 030              480 000",
            0.318,
            3.0 / 100.0,
            3.0 / 100.0 * (80.0 / 0.01 * 2.0),
            new TetrapolarSystem(0.04, 0.08)
        },

        {
            TetrapolarDerivativeResistance.of(new TetrapolarSystem(Metrics.fromMilli(10.0), Metrics.fromMilli(30.0))).dh(0.1).rho(8.1),
            "10 000   30 000     128 916        8 100              0 000",
            128.916,
            8.1,
            0.0,
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
            TetrapolarDerivativeResistance.milli().dh(0.1).system2(6.0).rho(1.0),
            "6000180002652610000000 30000180003978910000000",
            new double[] {1.0, 1.0},
            new double[] {0.0, 0.0}
        },
        {
            TetrapolarDerivativeResistance.milli().dh(0.2).system2(7.0).rho1(10.0).rho2(1.0).h(5.0),
            "700021000124160546124760 3500021000146710430220852",
            new double[] {5.46, 4.30},
            new double[] {24.760, 20.852}
        },
        {
            TetrapolarDerivativeResistance.milli().dh(0.3).system2(8.0).rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "80002400088617445420189 4000024000107985361917296",
            new double[] {4.45, 3.62},
            new double[] {20.189, 17.296}
        },
        {
            TetrapolarDerivativeResistance.milli().dh(0.01).system2(10.0)
                .ofOhms(1.0 / Math.PI, 2.0 / Math.PI,
                2.0 / Math.PI, 1.0 / Math.PI),
            "10000300000318002060000 50000300000637002740000",
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidOhms() {
    TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(0.01).ofOhms(1.0);
  }
}