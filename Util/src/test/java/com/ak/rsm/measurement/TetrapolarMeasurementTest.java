package com.ak.rsm.measurement;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.prediction.TetrapolarPrediction;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TetrapolarMeasurementTest {
  @DataProvider(name = "tetrapolar-measurements")
  public static Object[][] tetrapolarMeasurements() {
    return new Object[][] {
        {
            TetrapolarMeasurement.ofSI(0.01).system(1.0, 2.0).rho(900.1),
            "1000 000   2000 000      10 0       1959          900   27 0",
            900.1
        },
        {
            TetrapolarMeasurement.ofSI(0.1).system(2.0, 1.0).rho(900.2),
            "2000 000   1000 000      100 0       909          900   270 1",
            900.2
        },
        {
            TetrapolarMeasurement.ofMilli(0.01).system(10.0, 30.0).rho(8.1),
            "10 000   30 000      0 0       77          8 10   0 016",
            8.1
        },
        {
            TetrapolarMeasurement.ofMilli(0.1).system(50.0, 30.0).rho(8.2),
            "50 000   30 000      0 1       61          8 2   0 11",
            8.2
        },
        {
            TetrapolarMeasurement.ofMilli(0.1).system(10.0, 30.0).rho1(1.0).rho2(1.0).h(Math.random()),
            "10 000   30 000      0 1       36          1 00   0 020",
            1.0
        },
        {
            TetrapolarMeasurement.ofMilli(0.1).system(10.0, 30.0).rho1(10.0).rho2(1.0).h(5.0),
            "10 000   30 000      0 1       36          3 39   0 068",
            3.39
        },
        {
            TetrapolarMeasurement.ofMilli(0.1).system(10.0, 20.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "10 000   20 000      0 1       20          5 7   0 17",
            5.72,
        },

        {
            TetrapolarMeasurement.ofMilli(0.1).system(30.0, 60.0).ofOhms(1.0 / Math.PI),
            "30 000   60 000      0 1       85          0 0225   0 00023",
            9.0 / 400.0
        },
        {
            TetrapolarMeasurement.ofMilli(0.1).system(90.0, 30.0).ofOhms(1.0 / Math.PI),
            "90 000   30 000      0 1       154          0 0600   0 00040",
            3.0 / 50.0
        },
        {
            TetrapolarMeasurement.ofMilli(0.1).system(40.0, 80.0).ofOhms(1.0 / Math.PI),
            "40 000   80 000      0 1       124          0 0300   0 00023",
            3.0 / 100.0
        },

        {
            TetrapolarMeasurement.of(
                new InexactTetrapolarSystem(0.1, new TetrapolarSystem(Metrics.fromMilli(10.0), Metrics.fromMilli(30.0)))
            ).rho(8.1),
            "10 000   30 000      100 0       4          8   162 0",
            8.1
        },
    };
  }

  @Test(dataProvider = "tetrapolar-measurements")
  @ParametersAreNonnullByDefault
  public void test(Measurement measurement, String expected, @Nonnegative double resistivity) {
    Assert.assertEquals(measurement.toString().replaceAll("\\D", " ").strip(), expected, measurement.toString());
    Assert.assertEquals(measurement.resistivity(), resistivity, 0.01, measurement.toString());
    Assert.assertEquals(measurement.toPrediction(RelativeMediumLayers.SINGLE_LAYER, 1.0),
        TetrapolarPrediction.of(measurement, RelativeMediumLayers.SINGLE_LAYER, 1.0));
  }

  @Test
  public void testMerge() {
    Measurement m1 = TetrapolarMeasurement.ofSI(0.1).system(10.0, 30.0).rho(1.0);
    Assert.assertEquals(m1.merge(m1).resistivity(), 1.0, 0.001, m1.toString());
    Assert.assertEquals(m1.merge(m1).inexact().getApparentRelativeError(), 0.014, 0.001, m1.toString());

    Measurement m2 = TetrapolarMeasurement.ofSI(0.001).system(10.0, 30.0).rho(10.0);
    Assert.assertEquals(m1.merge(m2).toString(), m2.merge(m1).toString());

    Assert.assertEquals(m1.merge(m2).resistivity(), 9.91, 0.01);
    Assert.assertEquals(m2.merge(m1).inexact().getApparentRelativeError(), 0.002, 0.01);
  }

  @DataProvider(name = "tetrapolar-multi-measurements")
  public static Object[][] tetrapolarMultiMeasurements() {
    return new Object[][] {
        {
            TetrapolarMeasurement.milli(0.1).system2(6.0).rho(1.0),
            "60001800001181000033 300001800001311000022",
            new double[] {1.0, 1.0}
        },
        {
            TetrapolarMeasurement.milli(0.1).system2(7.0).rho1(10.0).rho2(1.0).h(5.0),
            "700021000012255016 350002100001384300082",
            new double[] {5.46, 4.30}
        },
        {
            TetrapolarMeasurement.milli(0.1).system2(8.0).rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "800024000012645011 400002400001453620060",
            new double[] {4.45, 3.62}
        },

        {
            TetrapolarMeasurement.milli(-0.1).withShiftError().system2(8.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "790024100012744011 399002410001453660061",
            new double[] {4.42, 3.65}
        },
        {
            TetrapolarMeasurement.milli(0.1).withShiftError().system2(8.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "810023900012645011 401002390001463580059",
            new double[] {4.49, 3.58}
        },

        {
            TetrapolarMeasurement.milli(0.1).system2(10.0).ofOhms(15.915, 23.873),
            "100003000001361000020 500003000001611000013",
            new double[] {1.0, 1.0}
        },
        {
            TetrapolarMeasurement.milli(0.1).system4(10.0).ofOhms(15.915, 23.873, 21.220, 25.465),
            "100003000001361000020 500003000001611000013 200004000001491000015 600004000001711000013",
            new double[] {1.0, 1.0, 1.0, 1.0}
        },
    };
  }

  @Test(dataProvider = "tetrapolar-multi-measurements")
  @ParametersAreNonnullByDefault
  public void testMulti(Collection<Measurement> ms, String expected, double[] resistivity) {
    Assert.assertEquals(
        ms.stream().map(
            m -> m.toString().replaceAll("\\D", " ").strip().replaceAll(Strings.SPACE, Strings.EMPTY)
        ).collect(Collectors.joining(Strings.SPACE)), expected, ms.toString());
    Assert.assertEquals(ms.stream().mapToDouble(Measurement::resistivity).toArray(), resistivity, 0.01, ms.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class, invocationCount = 3)
  public void testInvalidOhms() {
    TetrapolarMeasurement.milli(0.01).system2(10.0).ofOhms(DoubleStream.generate(Math::random)
        .limit(Math.random() > 0.5 ? 1 : 3).toArray());
  }
}