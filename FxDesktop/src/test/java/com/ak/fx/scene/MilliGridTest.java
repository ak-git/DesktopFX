package com.ak.fx.scene;

import org.testng.annotations.Test;

public class MilliGridTest {
  private MilliGridTest() {
  }

  @Test
  public static void testClose() {
    new MilliGrid().close();
  }
}