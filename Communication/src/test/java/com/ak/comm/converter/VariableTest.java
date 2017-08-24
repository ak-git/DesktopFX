package com.ak.comm.converter;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;
import tec.uom.se.quantity.Quantities;

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
    Assert.assertTrue(Variables.toString(ADCVariable.ADC, 1).startsWith("ADC = 1 "));
    Assert.assertTrue(Variables.toString(Quantities.getQuantity(1, AbstractUnit.ONE)).startsWith("1 "));
  }

  @Test
  public static void testToName() {
    Assert.assertTrue(Variables.toName(ADCVariable.ADC).startsWith("ADC, "));
  }
}