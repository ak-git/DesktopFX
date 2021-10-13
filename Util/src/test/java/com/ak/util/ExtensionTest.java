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
        {".txt"},
    };
  }

  @Test(dataProvider = "fileNames")
  public void testAttachTo(@Nonnull String name) {
    for (Extension e : EnumSet.complementOf(EnumSet.of(Extension.NONE))) {
      if (name.toUpperCase().endsWith(e.name())) {
        Assert.assertEquals(e.attachTo(name), ".%s".formatted(e.name().toLowerCase()));
      }
      else {
        Assert.assertEquals(e.attachTo(name), String.join(".", name, e.name().toLowerCase()));
      }
      Assert.assertTrue(e.is(e.attachTo(name)), name);
    }
    Assert.assertEquals(Extension.NONE.attachTo(name), name);
  }

  @Test(dataProvider = "fileNames")
  public void testReplace(@Nonnull String name) {
    for (Extension e : EnumSet.allOf(Extension.class)) {
      String expected = name;
      if (name.toUpperCase().endsWith(e.name())) {
        expected = name.substring(0, name.lastIndexOf(".%s".formatted(e.name().toLowerCase())));
      }
      Assert.assertEquals(e.clean(e.attachTo(name)), expected);
      Assert.assertEquals(e.clean(name), expected);
    }
  }
}