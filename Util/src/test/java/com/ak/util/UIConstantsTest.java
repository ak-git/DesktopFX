package com.ak.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UIConstantsTest {
  @Test
  public void testValues() {
    Assert.assertEquals(UIConstants.values().length, 0);
    Assert.assertEquals(UIConstants.UI_DELAY.getSeconds(), 3);
  }
}