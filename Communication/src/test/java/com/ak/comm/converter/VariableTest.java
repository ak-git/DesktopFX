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
  public static void testVisibleProperty() {
    Assert.assertFalse(OperatorVariables.OUT_MINUS.isVisible());
    Assert.assertTrue(OperatorVariables.OUT_PLUS.isVisible());

    EnumSet.allOf(OperatorVariables2.class).forEach(v -> Assert.assertTrue(v.isVisible(), Variables.toName(v)));
  }

  @Test
  public static void testToString() {
    Assert.assertEquals(Variables.toString(ADCVariable.ADC, 1), "ADC = 1 one");
  }

  @Test
  public static void testToName() {
    Assert.assertEquals(Variables.toName(ADCVariable.ADC), "ADC, one");
  }
}