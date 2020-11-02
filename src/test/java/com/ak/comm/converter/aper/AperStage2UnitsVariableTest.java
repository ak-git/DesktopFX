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
import com.ak.numbers.aper.AperSurfaceCoefficientsChannel1;
import com.ak.numbers.aper.AperSurfaceCoefficientsChannel2;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public class AperStage2UnitsVariableTest {
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

            new int[] {55615, 301400, 301400, -526617, -526616, 1042, 1042, 1296, 1726}},
    };
  }

  @Test(dataProvider = "variables")
  public void testApply(@Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    Converter<BufferFrame, AperStage2UnitsVariable> converter = LinkedConverter
        .of(new ToIntegerConverter<>(AperStage1Variable.class, 1000), AperStage2UnitsVariable.class);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 62 - 1; i++) {
      long count = converter.apply(bufferFrame).peek(ints -> {
        if (!processed.get()) {
          Assert.assertEquals(ints, outputInts, String.format("expected = %s, actual = %s", Arrays.toString(outputInts), Arrays.toString(ints)));
          processed.set(true);
        }
      }).count();
      if (processed.get()) {
        Assert.assertEquals(count, 1);
        break;
      }
    }
    Assert.assertTrue(processed.get(), "Data are not converted!");
    Assert.assertEquals(converter.getFrequency(), 1000, 0.1);
  }

  @Test
  public void testGetInputVariables() {
    int[] actual = EnumSet.allOf(AperStage2UnitsVariable.class).stream().mapToInt(value -> value.getInputVariables().size()).toArray();
    int[] expected = {2, 2, 2, 1, 1, 1, 1, 1, 1};
    Assert.assertEquals(actual, expected, Arrays.toString(actual));
  }

  @Test
  public void testGetUnit() {
    List<? extends Unit<?>> actual = EnumSet.allOf(AperStage2UnitsVariable.class).stream()
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
    List<Variable.Option> actual = EnumSet.allOf(AperStage2UnitsVariable.class).stream()
        .flatMap(aperStage2UnitsVariable -> aperStage2UnitsVariable.options().stream()).collect(Collectors.toList());
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
    double[] actual = EnumSet.allOf(AperStage2UnitsVariable.class).stream().mapToDouble(value -> value.filter().getDelay()).toArray();
    double[] expected = {0.0, 0.0, 0.0, 30.0, 30.0, 30.0, 30.0, 0.0, 0.0};
    Assert.assertEquals(actual, expected, Arrays.toString(actual));
  }

  @Test
  public void testInputVariablesClass() {
    Assert.assertTrue(EnumSet.allOf(AperStage2UnitsVariable.class).stream().map(AperStage2UnitsVariable::getInputVariablesClass)
        .allMatch(AperStage1Variable.class::equals));
  }

  @Test(enabled = false)
  public void testSplineSurface1() {
    SplineCoefficientsUtils.testSplineSurface1(AperSurfaceCoefficientsChannel1.class);
  }

  @Test(enabled = false)
  public void testSplineSurface2() {
    SplineCoefficientsUtils.testSplineSurface2(AperSurfaceCoefficientsChannel2.class);
  }
}