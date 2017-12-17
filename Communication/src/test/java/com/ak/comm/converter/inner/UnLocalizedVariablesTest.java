package com.ak.comm.converter.inner;

import com.ak.comm.converter.Variables;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UnLocalizedVariablesTest {
  private UnLocalizedVariablesTest() {
  }

  @Test
  public static void testToString() {
    Assert.assertEquals(Variables.toString(UnLocalizedVariables.MISSING_RESOURCE),
        UnLocalizedVariables.MISSING_RESOURCE.name());
  }
}