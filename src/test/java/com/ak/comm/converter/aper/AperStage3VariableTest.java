package com.ak.comm.converter.aper;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.measure.Unit;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.Variable;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public class AperStage3VariableTest {
  @DataProvider(name = "variables")
  public static Object[][] variables() {
    return new Object[][] {
        {new byte[] {1,
            (byte) 0x9a, (byte) 0x88, 0x01, 0,
            2, 0, 0, 0,
            (byte) 0xf1, 0x05, 0, 0,

            (byte) 0xff, (byte) 0xff, (byte) 0xff, 0,
            5, 0, 0, 0,
            (byte) 0xd0, 0x07, 0, 0},

            new int[] {79640, 301742, 301742, -527215, -527214, -512, -512, 1649, 2515}},
    };
  }

  @Test(dataProvider = "variables")
  public void testApply(@Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    Converter<BufferFrame, AperStage3Variable> converter = LinkedConverter
        .of(new ToIntegerConverter<>(AperStage1Variable.class, 1000), AperStage2UnitsVariable.class)
        .chainInstance(AperStage3Variable.class);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 500 - 1; i++) {
      long count = converter.apply(bufferFrame).peek(ints -> {
        if (!processed.get()) {
          Assert.assertEquals(ints, outputInts, "expected = %s, actual = %s".formatted(Arrays.toString(outputInts), Arrays.toString(ints)));
          processed.set(true);
        }
      }).count();
      if (processed.get()) {
        Assert.assertEquals(count, 4);
        break;
      }
    }
    Assert.assertTrue(processed.get(), "Data are not converted!");
    Assert.assertEquals(converter.getFrequency(), 1000, 0.1);
  }

  @Test
  public void testGetInputVariables() {
    Assert.assertTrue(EnumSet.allOf(AperStage3Variable.class).stream().mapToInt(value -> value.getInputVariables().size())
        .allMatch(value -> value == 1));
  }

  @Test
  public void testGetUnit() {
    List<? extends Unit<?>> actual = EnumSet.allOf(AperStage3Variable.class).stream()
        .map(DependentVariable::getUnit).collect(Collectors.toList());
    Assert.assertEquals(actual,
        Arrays.asList(
            MetricPrefix.MILLI(Units.OHM), MetricPrefix.MILLI(Units.OHM), MetricPrefix.MILLI(Units.OHM),
            MetricPrefix.MICRO(Units.VOLT), MetricPrefix.MICRO(Units.VOLT),
            MetricPrefix.MICRO(Units.VOLT), MetricPrefix.MICRO(Units.VOLT),
            Units.OHM, Units.OHM
        ),
        actual.toString()
    );
  }

  @Test
  public void testOptions() {
    List<Variable.Option> actual = EnumSet.allOf(AperStage3Variable.class).stream()
        .flatMap(v -> v.options().stream()).collect(Collectors.toList());
    Assert.assertEquals(actual,
        Arrays.asList(
            Variable.Option.VISIBLE, Variable.Option.VISIBLE, Variable.Option.VISIBLE,
            Variable.Option.VISIBLE, Variable.Option.VISIBLE,
            Variable.Option.VISIBLE, Variable.Option.VISIBLE,
            Variable.Option.TEXT_VALUE_BANNER, Variable.Option.TEXT_VALUE_BANNER
        ),
        actual.toString()
    );
  }

  @Test
  public void testFilterDelay() {
    double[] actual = EnumSet.allOf(AperStage3Variable.class).stream().mapToDouble(value -> value.filter().getDelay()).toArray();
    double[] expected = {157.5, 157.5, 157.5, 157.5, 157.5, 0.0, 0.0, 157.5, 157.5};
    Assert.assertEquals(actual, expected, Arrays.toString(actual));
  }

  @Test
  public void testInputVariablesClass() {
    Assert.assertTrue(EnumSet.allOf(AperStage3Variable.class).stream().map(AperStage3Variable::getInputVariablesClass)
        .allMatch(AperStage2UnitsVariable.class::equals));
  }
}