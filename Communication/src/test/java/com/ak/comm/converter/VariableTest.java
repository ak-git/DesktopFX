package com.ak.comm.converter;

import org.testng.Assert;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;

public class VariableTest {
  @Test
  public void testGetUnit() {
    Variable variable = new Variable() {
    };
    Assert.assertEquals(variable.getUnit(), AbstractUnit.ONE);
  }
}