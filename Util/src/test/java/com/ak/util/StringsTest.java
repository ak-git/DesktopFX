package com.ak.util;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.util.Strings.OHM_METRE;

public class StringsTest {
  @DataProvider(name = "strings")
  public static Object[][] strings() {
    return new Object[][] {
        {"CC1_1_2", "2"},
        {"CC_12", "12"},
        {"CC34", "34"},
        {"56", "56"},
        {"Pu", ""},
        {"1Pu", ""},
        {"P1u", ""},
        {"P_1_u", ""},
        {"", ""},
    };
  }

  @Test(dataProvider = "strings")
  public void testNumberSuffix(@Nonnull String toExtract, @Nonnull String expected) {
    Assert.assertEquals(Strings.numberSuffix(toExtract), expected);
  }

  @Test
  public void testArrayToString() {
    Assert.assertEquals(Strings.toString("%.3f", new double[] {1.2, 1.23, 1.2345}, Units.OHM), "{1,200; 1,230; 1,235} " + Units.OHM);
  }

  @Test
  public void testRhoH() {
    Assert.assertEquals(Strings.rho(2.1234), String.format("\u03c1 = %.3f %s", 2.123, OHM_METRE));
    Assert.assertEquals(Strings.rho1(2.1234), String.format("\u03c1\u2081 = %.3f %s", 2.123, OHM_METRE));
    Assert.assertEquals(Strings.rho2(20.1236), String.format("\u03c1\u2082 = %.3f %s", 20.124, OHM_METRE));
    Assert.assertEquals(Strings.h(0.21236, 2), String.format("h\u2082 = %.2f %s", 212.36, MetricPrefix.MILLI(Units.METRE)));
  }
}