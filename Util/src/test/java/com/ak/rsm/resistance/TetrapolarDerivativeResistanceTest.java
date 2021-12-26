package com.ak.rsm.resistance;

import javax.annotation.Nonnegative;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TetrapolarDerivativeResistanceTest {
  @DataProvider(name = "tetrapolar-resistivity")
  public static Object[][] tetrapolarResistivity() {
    return new Object[][] {
        {
            TetrapolarDerivativeResistance.si(1.0, 2.0).dh(0.1).rho(900.1),
            "1000 000   2000 000     382 014        900 100              0 000",
            382.014,
            900.1,
            0.0,
            new TetrapolarSystem(1.0, 2.0)
        },
        {
            TetrapolarDerivativeResistance.si(2.0, 1.0).dh(0.2).rho(900.2),
            "2000 000   1000 000     382 057        900 200              0 000",
            382.057,
            900.2,
            0.0,
            new TetrapolarSystem(1.0, 2.0)
        },
        {
            TetrapolarDerivativeResistance.milli(10.0, 30.0).dh(-0.1).rho(8.1),
            "10 000   30 000     128 916        8 100              0 000",
            128.916,
            8.1,
            0.0,
            new TetrapolarSystem(0.01, 0.03)
        },
        {
            TetrapolarDerivativeResistance.milli(50.0, 30.0).dh(0.0).rho(8.2),
            "50 000   30 000     195 761        8 200              0 000",
            195.761,
            8.2,
            0.0,
            new TetrapolarSystem(0.03, 0.05)
        },
        {
            TetrapolarDerivativeResistance.milli(10.0, 30.0).dh(0.1).rho1(1.0).rho2(1.0).h(Math.random()),
            "10 000   30 000     15 915        1 000              0 000",
            15.915,
            1.0,
            0.0,
            new TetrapolarSystem(0.01, 0.03)
        },
        {
            TetrapolarDerivativeResistance.milli(10.0, 30.0).dh(0.1).rho1(10.0).rho2(1.0).h(5.0),
            "10 000   30 000     53 901        3 387              31 312",
            53.901,
            3.39,
            31.312,
            new TetrapolarSystem(0.01, 0.03)
        },
        {
            TetrapolarDerivativeResistance.milli(10.0, 20.0).dh(0.1).rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "10 000   20 000     242 751        5 720              13 875",
            242.751,
            5.72,
            13.875,
            new TetrapolarSystem(0.01, 0.02)
        },

        {
            TetrapolarDerivativeResistance.milli(30.0, 60.0).dh(0.01).ofOhms(1.0 / Math.PI),
            "30 000   60 000     0 318        0 023              0 000",
            0.318,
            9.0 / 400.0,
            0.0,
            new TetrapolarSystem(0.03, 0.06)
        },
        {
            TetrapolarDerivativeResistance.milli(90.0, 30.0).dh(0.01).ofOhms(1.0 / Math.PI),
            "90 000   30 000     0 318        0 060              0 000",
            0.318,
            3.0 / 50.0,
            0.0,
            new TetrapolarSystem(0.09, 0.03)
        },
        {
            TetrapolarDerivativeResistance.milli(40.0, 80.0).dh(0.01).ofOhms(1.0 / Math.PI),
            "40 000   80 000     0 318        0 030              0 000",
            0.318,
            3.0 / 100.0,
            0.0,
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
}