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
            TetrapolarResistance.milli(10.0, 30.0).rho1(1.0).rho2(1.0).h(Math.random()),
            "10 000   30 000     15 915        1 000",
            15.915,
            1.0,
        },
        {
            TetrapolarResistance.milli(10.0, 30.0).rho1(10.0).rho2(1.0).h(5.0),
            "10 000   30 000     53 901        3 387",
            53.901,
            3.39,
        },
        {
            TetrapolarResistance.milli(10.0, 20.0).rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "10 000   20 000     242 751        5 720",
            242.751,
            5.72,
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
  public void test(Resistance t, String expected, @Nonnegative double ohms, @Nonnegative double resistivity) {
    Assert.assertEquals(t.toString().replaceAll("\\D", " ").strip(), expected, t.toString());
    Assert.assertEquals(t.ohms(), ohms, 0.01, t.toString());
    Assert.assertEquals(t.resistivity(), resistivity, 0.01, t.toString());
  }

  @DataProvider(name = "tetrapolar-multi-resistivity")
  public static Object[][] tetrapolarMultiResistivity() {
    return new Object[][] {
        {
            TetrapolarResistance.milli().system2(6.0).rho(1.0),
            "600018000265261000 3000018000397891000",
            new double[] {1.0, 1.0}
        },
        {
            TetrapolarResistance.milli().system2(7.0).rho1(10.0).rho2(1.0).h(5.0),
            "7000210001241605461 35000210001467104302",
            new double[] {5.46, 4.30}
        },
        {
            TetrapolarResistance.milli().system2(8.0).rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "800024000886174454 40000240001079853619",
            new double[] {4.45, 3.62}
        },

        {
            TetrapolarResistance.milli().system4(6.0).rho(1.0),
            "600018000265261000 3000018000397891000 1200024000353681000 3600024000424411000",
            new double[] {1.0, 1.0, 1.0, 1.0}
        },
        {
            TetrapolarResistance.milli().system4(7.0).rho1(10.0).rho2(1.0).h(5.0),
            "7000210001241605461 35000210001467104302 14000280001415264668 42000280001492924104",
            new double[] {5.461, 4.302, 4.668, 4.104}
        },
        {
            TetrapolarResistance.milli().system4(8.0).rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "800024000886174454 40000240001079853619 16000320001031643889 48000320001103903468",
            new double[] {4.454, 3.619, 3.889, 3.468}
        },
    };
  }

  @Test(dataProvider = "tetrapolar-multi-resistivity")
  @ParametersAreNonnullByDefault
  public void testMulti(Collection<Resistance> ms, String expected, double[] resistivity) {
    Assert.assertEquals(
        ms.stream().map(
            m -> m.toString().replaceAll("\\D", " ").strip().replaceAll(Strings.SPACE, Strings.EMPTY)
        ).collect(Collectors.joining(Strings.SPACE)), expected, ms.toString());
    Assert.assertEquals(ms.stream().mapToDouble(Resistance::resistivity).toArray(), resistivity, 0.01, ms.toString());
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testInvalidOhms() {
    TetrapolarResistance.milli().system2(10.0).ofOhms(1.0, 2.0, 3.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidOhms2() {
    TetrapolarResistance.milli(40.0, 80.0).ofOhms(1.0 / Math.PI, 1.0 / Math.PI);
  }
}