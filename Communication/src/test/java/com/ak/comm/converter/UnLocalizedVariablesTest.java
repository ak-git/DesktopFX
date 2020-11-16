package com.ak.comm.converter;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UnLocalizedVariablesTest {
  @Test
  public void testToString() {
    Assert.assertEquals(Variables.toString(UnLocalizedVariables.MISSING_RESOURCE),
        UnLocalizedVariables.MISSING_RESOURCE.name());
  }
}