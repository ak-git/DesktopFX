package com.ak.util;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
  public void testRhoPhi() {
    Assert.assertEquals(Strings.rho(2.1234), "\u03c1 = %.3f %s".formatted(2.123, OHM_METRE));
    Assert.assertEquals(Strings.dRhoByPhi(1.21), "d\u03c1/d\u03C8 = %.3f %s".formatted(1.21, OHM_METRE));
  }

  @Test
  public void testRho() {
    Assert.assertEquals(Strings.rho(1, 2.1234), "ρ₁ = 2.1234 Ω·m");
  }
}