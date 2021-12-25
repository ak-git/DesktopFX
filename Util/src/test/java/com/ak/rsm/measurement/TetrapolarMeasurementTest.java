package com.ak.rsm.measurement;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.resistance.TetrapolarResistance;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TetrapolarMeasurementTest {
  @DataProvider(name = "tetrapolar-measurements")
  public static Object[][] tetrapolarMeasurements() {
    return new Object[][] {
        {
            TetrapolarMeasurement.si(0.01).system(1.0, 2.0).rho(900.1),
            "1000 000   2000 000      10 0       1959          900   27 0",
            900.1
        },
        {
            TetrapolarMeasurement.si(0.1).system(2.0, 1.0).rho(900.2),
            "2000 000   1000 000      100 0       909          900   270 1",
            900.2
        },
        {
            TetrapolarMeasurement.milli(0.01).system(10.0, 30.0).rho(8.1),
            "10 000   30 000      0 0       77          8 10   0 016",
            8.1
        },
        {
            TetrapolarMeasurement.milli(0.1).system(50.0, 30.0).rho(8.2),
            "50 000   30 000      0 1       61          8 2   0 11",
            8.2
        },
        {
            TetrapolarMeasurement.milli(0.1).system(10.0, 30.0).rho1(1.0).rho2(1.0).h(Math.random()).build(),
            "10 000   30 000      0 1       36          1 00   0 020",
            1.0
        },
        {
            TetrapolarMeasurement.milli(0.1).system(10.0, 30.0).rho1(10.0).rho2(1.0).h(5.0).build(),
            "10 000   30 000      0 1       36          3 39   0 068",
            3.39
        },

        {
            TetrapolarMeasurement.milli(0.1).system(30.0, 60.0).ofOhms(1.0 / Math.PI),
            "30 000   60 000      0 1       85          0 0225   0 00023",
            9.0 / 400.0
        },
        {
            TetrapolarMeasurement.milli(0.1).system(90.0, 30.0).ofOhms(1.0 / Math.PI),
            "90 000   30 000      0 1       154          0 0600   0 00040",
            3.0 / 50.0
        },
        {
            TetrapolarMeasurement.milli(0.1).system(40.0, 80.0).ofOhms(1.0 / Math.PI),
            "40 000   80 000      0 1       124          0 0300   0 00023",
            3.0 / 100.0
        },
    };
  }

  @Test(dataProvider = "tetrapolar-measurements")
  @ParametersAreNonnullByDefault
  public void test(Measurement measurement, String expected, double resistivity) {
    Assert.assertEquals(measurement.toString().replaceAll("\\D", " ").strip(), expected, measurement.toString());
    Assert.assertEquals(measurement.resistivity(), resistivity, 0.01, measurement.toString());
  }

  @Test
  public void testMerge() {
    Measurement m1 = TetrapolarMeasurement.milli(0.1).system(10.0, 30.0).rho(1.0);
    Assert.assertEquals(m1.merge(m1).resistivity(), 1.0, 0.001, m1.toString());
    Assert.assertEquals(m1.merge(m1).system().getApparentRelativeError(), 0.014, 0.001, m1.toString());

    Measurement m2 = TetrapolarMeasurement.milli(0.001).system(10.0, 30.0).rho(10.0);
    Assert.assertEquals(m1.merge(m2).toString(), m2.merge(m1).toString());

    Assert.assertEquals(m1.merge(m2).resistivity(), 9.91, 0.01);
    Assert.assertEquals(m2.merge(m1).system().getApparentRelativeError(), 0.002, 0.01);
  }

  @DataProvider(name = "tetrapolar-measurements-invalid")
  public static Object[][] tetrapolarMeasurementsInvalid() {
    return new Object[][] {
        {TetrapolarMeasurement.si(0.1).system(1.0, 2.0).rho1(900.1)},
        {TetrapolarMeasurement.milli(0.0).system(1.0, 2.0).rho1(900.1).h(1.0)},
        {TetrapolarMeasurement.milli(0.0).system(1.0, 2.0).rho1(900.1).rho2(1.0)},
    };
  }

  @Test(dataProvider = "tetrapolar-measurements-invalid", expectedExceptions = IllegalStateException.class)
  public void testInvalid(@Nonnull TetrapolarResistance.LayersBuilder<Measurement> builder) {
    builder.build();
  }
}