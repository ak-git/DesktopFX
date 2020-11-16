package com.ak.util;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ExtensionTest {
  @DataProvider(name = "fileNames")
  public static Object[][] fileNames() {
    return new Object[][] {
        {"COEFF"},
        {""},
        {"file.test."},
        {".ignore"},
    };
  }

  @Test(dataProvider = "fileNames")
  public void testAttachTo(@Nonnull String name) {
    for (Extension e : EnumSet.complementOf(EnumSet.of(Extension.NONE))) {
      Assert.assertEquals(e.attachTo(name), String.join(".", name, e.name().toLowerCase()));
    }
    Assert.assertEquals(Extension.NONE.attachTo(name), name);
  }
}