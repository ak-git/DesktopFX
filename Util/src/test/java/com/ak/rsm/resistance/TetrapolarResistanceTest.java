package com.ak.rsm.resistance;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TetrapolarResistanceTest {
  @DataProvider(name = "tetrapolar-resistivity")
  public static Object[][] tetrapolarResistivity() {
    return new Object[][] {
        {
            TetrapolarResistance.si(1.0, 2.0).rho(900.1),
            "1000 000   2000 000     382 014        900 100",
            382.014,
            900.1,
        },
        {
            TetrapolarResistance.si(2.0, 1.0).rho(900.2),
            "2000 000   1000 000     382 057        900 200",
            382.057,
            900.2,
        },
        {
            TetrapolarResistance.milli(10.0, 30.0).rho(8.1),
            "10 000   30 000     128 916        8 100",
            128.916,
            8.1,
        },
        {
            TetrapolarResistance.milli(50.0, 30.0).rho(8.2),
            "50 000   30 000     195 761        8 200",
            195.761,
            8.2,
        },
        {
            TetrapolarResistance.milli(10.0, 30.0).rho1(1.0).rho2(1.0).h(Math.random()).build(),
            "10 000   30 000     15 915        1 000",
            15.915,
            1.0,
        },
        {
            TetrapolarResistance.milli(10.0, 30.0).rho1(10.0).rho2(1.0).h(5.0).build(),
            "10 000   30 000     53 901        3 387",
            53.901,
            3.39,
        },

        {
            TetrapolarResistance.milli(30.0, 60.0).ofOhms(1.0 / Math.PI),
            "30 000   60 000     0 318        0 023",
            0.318,
            9.0 / 400.0,
        },
        {
            TetrapolarResistance.milli(90.0, 30.0).ofOhms(1.0 / Math.PI),
            "90 000   30 000     0 318        0 060",
            0.318,
            3.0 / 50.0,
        },
        {
            TetrapolarResistance.milli(40.0, 80.0).ofOhms(1.0 / Math.PI),
            "40 000   80 000     0 318        0 030",
            0.318,
            3.0 / 100.0,
        },

        {
            TetrapolarResistance.of(new TetrapolarSystem(Metrics.fromMilli(10.0), Metrics.fromMilli(30.0))).rho(8.1),
            "10 000   30 000     128 916        8 100",
            128.916,
            8.1,
        },
    };
  }

  @Test(dataProvider = "tetrapolar-resistivity")
  @ParametersAreNonnullByDefault
  public void test(Resistance tResistance, String expected, double ohms, double resistivity) {
    Assert.assertEquals(tResistance.toString().replaceAll("\\D", " ").strip(), expected, tResistance.toString());
    Assert.assertEquals(tResistance.ohms(), ohms, 0.01, tResistance.toString());
    Assert.assertEquals(tResistance.resistivity(), resistivity, 0.01, tResistance.toString());
  }

  @DataProvider(name = "tetrapolar-resistivity-invalid")
  public static Object[][] tetrapolarResistivityInvalid() {
    return new Object[][] {
        {TetrapolarResistance.si(1.0, 2.0).rho1(900.1)},
        {TetrapolarResistance.milli(1.0, 2.0).rho1(900.1).h(1.0)},
        {TetrapolarResistance.milli(1.0, 2.0).rho1(900.1).rho2(1.0)},
    };
  }

  @Test(dataProvider = "tetrapolar-resistivity-invalid", expectedExceptions = IllegalStateException.class)
  public void testInvalid(@Nonnull TetrapolarResistance.LayersBuilder<Resistance> builder) {
    builder.build();
  }
}