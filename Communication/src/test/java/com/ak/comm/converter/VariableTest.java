package com.ak.comm.converter;

import com.ak.comm.converter.missing.MissingResourceVariables;
import com.ak.comm.logging.LogTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.AbstractUnit;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import javax.annotation.Nonnegative;
import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.EnumSet;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class VariableTest {
  private static final Logger LOGGER = Logger.getLogger(Variables.class.getName());

  @Test
  void testGetUnit() {
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
    assertThat(variable.getUnit()).isEqualTo(AbstractUnit.ONE);
    assertThat(variable.options()).containsExactly(Variable.Option.VISIBLE);
  }

  @Test
  void testGetDependentUnit() {
    assertThat(OperatorVariables2.OUT.getUnit()).isEqualTo(AbstractUnit.ONE);
  }

  @Test
  void testInvalidGetVariables() {
    DependentVariable<OperatorVariables, ADCVariable> variable = new DependentVariable<>() {
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

      @Override
      public Class<OperatorVariables> getInputVariablesClass() {
        return OperatorVariables.class;
      }
    };

    assertThatIllegalArgumentException().isThrownBy(variable::getInputVariables)
        .withMessageMatching(".*No enum constant.*InvalidName");
  }

  @Test
  void testVisibleProperty() {
    assertThat(SingleVariables.E1.options()).contains(Variable.Option.VISIBLE);
    assertThat(OperatorVariables.OUT_PLUS.options()).contains(Variable.Option.VISIBLE);
    assertThat(OperatorVariables.OUT_PLUS.indexBy(Variable.Option.VISIBLE)).isZero();
    assertThat(OperatorVariables.OUT_MINUS.options()).isEmpty();
    assertThat(OperatorVariables.OUT_MINUS.indexBy(Variable.Option.VISIBLE)).isNegative();
    assertThat(OperatorVariables.OUT_DIV.options()).contains(Variable.Option.VISIBLE);
    assertThat(OperatorVariables.OUT_DIV.indexBy(Variable.Option.VISIBLE)).isEqualTo(1);
  }

  @ParameterizedTest
  @EnumSource(value = OperatorVariables2.class)
  void testOperatorVariables2(Variable<OperatorVariables2> variable) {
    assertThat(variable.options()).containsExactly(Variable.Option.VISIBLE, Variable.Option.TEXT_VALUE_BANNER);
  }

  @Test
  void testToString() {
    String adc = Variables.toString(ADCVariable.ADC, 10000);
    assertThat(adc).contains(" = ").endsWith(String.format(Locale.getDefault(), "%,d one", 10000));
    assertThat(Variables.toString(Quantities.getQuantity(1, AbstractUnit.ONE))).startsWith("1 ");

    assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, Level.CONFIG,
        () -> assertThat(Variables.toString(MissingResourceVariables.NONE)).isEqualTo(MissingResourceVariables.NONE.name()),
        logRecord -> {
          assertThat(logRecord.getMessage()).contains(MissingResourceVariables.NONE.name());
          assertNotNull(logRecord.getThrown());
        })
    );

    assertFalse(LogTestUtils.isSubstituteLogLevel(LOGGER, Level.CONFIG,
        () -> assertThat(Variables.toString(TwoVariables.V1)).isEqualTo("Variable Name 1"),
        logRecord -> fail(logRecord.getMessage()))
    );

    assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, Level.WARNING,
        () -> assertThat(Variables.toString(TwoVariables.V2)).isEqualTo(TwoVariables.V2.name()),
        logRecord -> {
          assertThat(logRecord.getMessage()).contains(TwoVariables.V2.name());
          assertNull(logRecord.getThrown());
        })
    );
  }

  static Stream<Arguments> formatValues() {
    return Stream.of(
        arguments(1234, MetricPrefix.CENTI(Units.HERTZ), 1, "%,.2f Hz".formatted(12.34)),
        arguments(-1234, MetricPrefix.CENTI(Units.HERTZ), 10, "%,.1f Hz".formatted(-12.3)),
        arguments(1234, MetricPrefix.CENTI(Units.HERTZ), 100, "%.0f Hz".formatted(12.0)),
        arguments(-1234, Units.HERTZ, 1, "%,.3f kHz".formatted(-1.234)),
        arguments(1234, Units.HERTZ, 10, "%,.2f kHz".formatted(1.23)),
        arguments(-1234, Units.HERTZ, 100, "%,.1f kHz".formatted(-1.2)),
        arguments(1234, Units.HERTZ, 1000, "%.0f kHz".formatted(1.0)),
        arguments(-123, Units.HERTZ, 1, "%.0f Hz".formatted(-123.0)),
        arguments(-3140, Units.VOLT, 1, "%,.2f kV".formatted(-3.14)),
        arguments(3100, Units.VOLT, 1, "%,.1f kV".formatted(3.1)),
        arguments(-3000, Units.VOLT, 1, "%.0f kV".formatted(-3.0)),
        arguments(0, Units.VOLT, 1, "%.0f V".formatted(0.0)),
        arguments(0, Units.VOLT, 1000, "%.0f kV".formatted(0.0)),
        arguments(1, Units.OHM.multiply(Units.METRE), 10, "%.0f Ω·m".formatted(1.0)),
        arguments(41235, MetricPrefix.MILLI(Units.OHM).multiply(MetricPrefix.DECI(Units.METRE)), 10, "%,.0f mΩ·m".formatted(4123.5))
    );
  }

  @ParameterizedTest
  @MethodSource("formatValues")
  void testFormatValues(int value, Unit<?> unit, @Nonnegative int scaleFactor10, String expected) {
    assertThat(Variables.toString(value, unit, scaleFactor10)).isEqualTo(expected);
  }

  @Test
  void testTimeVariable() {
    assertThat(TimeVariable.values()).hasSize(1);
    assertThat(EnumSet.allOf(TimeVariable.class)).isNotEmpty()
        .allSatisfy(timeVariable -> assertThat(timeVariable.getUnit()).isEqualTo(Units.SECOND));
  }

  static Stream<Arguments> conversion() {
    return Stream.of(
        arguments(Units.OHM.multiply(Units.METRE), MetricPrefix.MILLI(Units.OHM).multiply(Units.METRE)),
        arguments(Units.METRE, MetricPrefix.CENTI(Units.METRE)),
        arguments(Units.METRE, MetricPrefix.DECI(Units.METRE)),
        arguments(Units.METRE, MetricPrefix.MILLI(Units.METRE)),
        arguments(MetricPrefix.MILLI(Units.METRE), MetricPrefix.MICRO(Units.METRE)),
        arguments(MetricPrefix.KILO(Units.METRE), MetricPrefix.KILO(Units.METRE)),
        arguments(MetricPrefix.DEKA(Units.METRE), MetricPrefix.DEKA(Units.METRE))
    );
  }

  @ParameterizedTest
  @MethodSource("conversion")
  <Q extends Quantity<Q>> void testTryToUp3(Unit<Q> expected, Unit<Q> toConvert) {
    assertEquals(expected, Variables.tryToUp3(toConvert));
  }
}