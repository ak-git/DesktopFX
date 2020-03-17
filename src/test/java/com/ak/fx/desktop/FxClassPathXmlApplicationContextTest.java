package com.ak.fx.desktop;

import javax.annotation.Nonnull;

import com.ak.util.PropertiesSupport;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FxClassPathXmlApplicationContextTest {
  @DataProvider(name = "contexts")
  public static Object[][] contexts() {
    return new Object[][] {
        {"aper.calibration"},
        {"aper"},
        {"nmis"},
        {"nmisr"},
        {"rcm.calibration"},
        {"rcm"},
        {"aper,aper2"},
    };
  }

  @Test(dataProvider = "contexts")
  public void testContext(@Nonnull String contextName) {
    PropertiesSupport.CONTEXT.update(contextName);
    for (String context : PropertiesSupport.CONTEXT.split()) {
      Assert.assertEquals(new FxClassPathXmlApplicationContext(FxClassPathXmlApplicationContext.class, context).getApplicationName(), "");
    }
    PropertiesSupport.CONTEXT.update(Strings.EMPTY);
  }
}