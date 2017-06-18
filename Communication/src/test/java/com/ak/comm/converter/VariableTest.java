package com.ak.comm.converter;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;

public class VariableTest {
  private VariableTest() {
  }

  @Test
  public static void testGetUnit() {
    Variable<ADCVariable> variable = new Variable<ADCVariable>() {
      @Override
      public String name() {
        return Variable.class.getSimpleName();
      }

      @Override
      public Class<ADCVariable> getDeclaringClass() {
        return ADCVariable.class;
      }
    };
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
    DependentVariable<OperatorVariables, ADCVariable> variable = new DependentVariable<OperatorVariables, ADCVariable>() {
      @Nonnull
      @Override
      public String name() {
        return "InvalidName";
      }

      @Override
      public Class<ADCVariable> getDeclaringClass() {
        return ADCVariable.class;
      }

      @Nonnull
      @Override
      public Class<OperatorVariables> getInputVariablesClass() {
        return OperatorVariables.class;
      }
    };

    variable.getInputVariables();
  }

  @Test
  public static void testDisplayProperty() {
    Assert.assertFalse(Variables.isDisplay(OperatorVariables.OUT_MINUS));
    Assert.assertTrue(Variables.isDisplay(OperatorVariables.OUT_PLUS));

    EnumSet.allOf(OperatorVariables2.class).forEach(v -> Assert.assertTrue(Variables.isDisplay(v), v.toName()));
  }
}