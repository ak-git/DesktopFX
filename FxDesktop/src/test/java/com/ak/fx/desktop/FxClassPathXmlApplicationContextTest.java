package com.ak.fx.desktop;

import java.util.Optional;

import javax.annotation.Nullable;

import com.ak.util.Strings;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FxClassPathXmlApplicationContextTest {
  private FxClassPathXmlApplicationContextTest() {
  }

  @DataProvider(name = "contexts")
  public static Object[][] contexts() {
    return new Object[][] {
        {null},
        {""},
        {"aper"}
    };
  }


  @Test(dataProvider = "contexts")
  public static void testContext(@Nullable String contextName) {
    Assert.assertEquals(new FxClassPathXmlApplicationContext(contextName).getApplicationName(),
        Optional.ofNullable(contextName).orElse(Strings.EMPTY));
  }

  @Test(expectedExceptions = BeanDefinitionStoreException.class,
      expectedExceptionsMessageRegExp = ".*cannot be opened because it does not exist")
  public static void testInvalidContext() {
    new FxClassPathXmlApplicationContext(Double.toString(Math.PI));
  }
}