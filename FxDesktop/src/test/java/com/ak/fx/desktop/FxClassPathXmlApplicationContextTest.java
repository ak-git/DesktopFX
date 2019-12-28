package com.ak.fx.desktop;

import javax.annotation.Nonnull;

import com.ak.util.PropertiesSupport;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FxClassPathXmlApplicationContextTest {
  private FxClassPathXmlApplicationContextTest() {
  }

  @DataProvider(name = "contexts")
  public static Object[][] contexts() {
    return new Object[][] {
        {"aper.calibration"},
        {"aper.sincos"},
        {"aper.sinsin"},
        {"nmis"},
        {"nmisr"},
        {"rcm.calibration"},
        {"rcm"},
    };
  }

  @Test(dataProvider = "contexts")
  public static void testContext(@Nonnull String contextName) {
    PropertiesSupport.CONTEXT.update(contextName);
    Assert.assertEquals(new FxClassPathXmlApplicationContext(FxClassPathXmlApplicationContext.class).getApplicationName(), "");
    PropertiesSupport.CONTEXT.update(Strings.EMPTY);
  }
}