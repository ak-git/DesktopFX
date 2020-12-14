package com.ak.comm.converter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.measure.Unit;

import com.ak.comm.logging.LogTestUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public class VariableTest {
  private static final Logger LOGGER = Logger.getLogger(Variables.class.getName());

  @Test
  public void testGetUnit() {
    Variable<ADCVariable> variable = new Variable<>() {
      @Override
      public String name() {
        return Variable.class.getSimpleName();
      }

      @Override
      public int ordinal() {
        return 0;
      }

      @Override
      public Class<ADCVariable> getDeclaringClass() {
        return ADCVariable.class;
      }
    };
    Assert.assertEquals(variable.getUnit(), AbstractUnit.ONE);
    Assert.assertEquals(variable.options(), Collections.singleton(Variable.Option.VISIBLE), variable.name());
  }

  @Test
  public void testGetDependentUnit() {
    Assert.assertEquals(OperatorVariables2.OUT.getUnit(), AbstractUnit.ONE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*No enum constant.*InvalidName")
  public void testInvalidGetVariables() {
    DependentVariable<OperatorVariables, ADCVariable> variable = new DependentVariable<>() {
      @Nonnull
      @Override
      public String name() {
        return "InvalidName";
      }

      @Override
      public int ordinal() {
        return 0;
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
  public void testVisibleProperty() {
    Assert.assertTrue(SingleVariables.E1.options().contains(Variable.Option.VISIBLE));
    Assert.assertTrue(OperatorVariables.OUT_PLUS.options().contains(Variable.Option.VISIBLE));
    Assert.assertEquals(OperatorVariables.OUT_PLUS.indexBy(Variable.Option.VISIBLE), 0);
    Assert.assertFalse(OperatorVariables.OUT_MINUS.options().contains(Variable.Option.VISIBLE));
    Assert.assertEquals(OperatorVariables.OUT_MINUS.indexBy(Variable.Option.VISIBLE), -1);
    Assert.assertTrue(OperatorVariables.OUT_DIV.options().contains(Variable.Option.VISIBLE));
    Assert.assertEquals(OperatorVariables.OUT_DIV.indexBy(Variable.Option.VISIBLE), 1);

    EnumSet.allOf(OperatorVariables2.class).forEach(v ->
        Assert.assertEquals(v.options(), EnumSet.of(Variable.Option.VISIBLE, Variable.Option.TEXT_VALUE_BANNER), Variables.toName(v)));
  }

  @Test
  public void testToString() {
    String adc = Variables.toString(ADCVariable.ADC, 10000);
    Assert.assertTrue(adc.startsWith("ADC = "), adc);
    Assert.assertTrue(adc.endsWith(String.format(Locale.getDefault(), "%,d one", 10000)), adc);
    Assert.assertTrue(Variables.toString(Quantities.getQuantity(1, AbstractUnit.ONE)).startsWith("1 "));

    Assert.assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, Level.CONFIG,
        () -> Assert.assertEquals(Variables.toString(ADCVariable.ADC), ADCVariable.ADC.name()),
        logRecord -> {
          Assert.assertTrue(logRecord.getMessage().contains(ADCVariable.ADC.name()));
          Assert.assertNull(logRecord.getThrown());
        }));

    Assert.assertFalse(LogTestUtils.isSubstituteLogLevel(LOGGER, Level.CONFIG,
        () -> Assert.assertEquals(Variables.toString(TwoVariables.V1), "Variable Name 1"),
        logRecord -> Assert.fail(logRecord.getMessage())));

    Assert.assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, Level.CONFIG,
        () -> Assert.assertEquals(Variables.toString(TwoVariables.V2), TwoVariables.V2.name()), logRecord -> {
          Assert.assertTrue(logRecord.getMessage().contains(TwoVariables.V2.name()));
          Assert.assertNull(logRecord.getThrown());
        }));
  }

  @Test
  public void testToName() {
    Assert.assertTrue(Variables.toName(ADCVariable.ADC).startsWith("ADC, "));
  }

  @DataProvider(name = "formatValues")
  public static Object[][] formatValues() {
    return new Object[][] {
        {1234, MetricPrefix.CENTI(Units.HERTZ), 1, "%,.2f Hz".formatted(12.34)},
        {-1234, MetricPrefix.CENTI(Units.HERTZ), 10, "%,.1f Hz".formatted(-12.3)},
        {1234, MetricPrefix.CENTI(Units.HERTZ), 100, "%.0f Hz".formatted(12.0)},
        {-1234, Units.HERTZ, 1, "%,.3f kHz".formatted(-1.234)},
        {1234, Units.HERTZ, 10, "%,.2f kHz".formatted(1.23)},
        {-1234, Units.HERTZ, 100, "%,.1f kHz".formatted(-1.2)},
        {1234, Units.HERTZ, 1000, "%.0f kHz".formatted(1.0)},
        {-123, Units.HERTZ, 1, "%.0f Hz".formatted(-123.0)},
        {-3140, Units.VOLT, 1, "%,.2f kV".formatted(-3.14)},
        {3100, Units.VOLT, 1, "%,.1f kV".formatted(3.1)},
        {-3000, Units.VOLT, 1, "%.0f kV".formatted(-3.0)},
        {0, Units.VOLT, 1, "%.0f V".formatted(0.0)},
        {0, Units.VOLT, 1000, "%.0f kV".formatted(0.0)},
        {1, Units.OHM.multiply(Units.METRE), 10, "%.0f Ω·m".formatted(1.0)},
        {41235, MetricPrefix.MILLI(Units.OHM).multiply(MetricPrefix.DECI(Units.METRE)), 10, "%,.0f mΩ·m".formatted(4123.5)}
    };
  }

  @Test(dataProvider = "formatValues")
  public void testFormatValues(int value, @Nonnull Unit<?> unit, @Nonnegative int scaleFactor10, @Nonnull String expected) {
    Assert.assertEquals(Variables.toString(value, unit, scaleFactor10), expected);
  }
}