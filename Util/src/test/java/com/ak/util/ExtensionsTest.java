package com.ak.util;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ExtensionsTest {
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
    for (Extensions e : Extensions.values()) {
      Assert.assertEquals(e.attachTo(name), String.join(".", name, e.name().toLowerCase()));
    }
  }
}