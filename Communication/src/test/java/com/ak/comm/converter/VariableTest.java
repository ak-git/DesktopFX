package com.ak.comm.converter;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;

public class VariableTest {
  private VariableTest() {
  }

  @Test
  public static void testGetUnit() {
    Variable variable = Variable.class::getSimpleName;
    Assert.assertEquals(variable.getUnit(), AbstractUnit.ONE);
    Assert.assertEquals(variable.toString(1), "Variable = 1 one");
    Assert.assertEquals(variable.toName(), "Variable, one");
  }

  @Test
  public static void testGetDependentUnit() {
    Assert.assertEquals(OperatorVariables2.OUT.getUnit(), AbstractUnit.ONE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*No enum constant.*InvalidName")
  public static void testInvalidGetVariables() {
    DependentVariable<OperatorVariables> variable = new DependentVariable<OperatorVariables>() {
      @Nonnull
      @Override
      public String name() {
        return "InvalidName";
      }

      @Nonnull
      @Override
      public Class<OperatorVariables> getInputVariablesClass() {
        return OperatorVariables.class;
      }
    };

    variable.getInputVariables();
  }
}