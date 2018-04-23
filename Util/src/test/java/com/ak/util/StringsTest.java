package com.ak.util;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class StringsTest {
  @DataProvider(name = "strings")
  public static Object[][] aperCoefficients() {
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
}