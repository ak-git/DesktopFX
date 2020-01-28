package com.ak.comm.converter.aper;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
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

public final class AperTest {
  private AperTest() {
  }

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

            new int[] {55699, -526617, 1325}},
    };
  }

  @Test(dataProvider = "variables")
  public static void testApply(@Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    Converter<BufferFrame, AperOutVariable> converter = new LinkedConverter<>(
        new ToIntegerConverter<>(AperInVariable.class, 1000), AperOutVariable.class);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 2000 - 1; i++) {
      int finalI = i;
      long count = converter.apply(bufferFrame).peek(ints -> {
        if (finalI > 1900) {
          if (!processed.get()) {
            Assert.assertEquals(ints, outputInts, String.format("expected = %s, actual = %s", Arrays.toString(outputInts), Arrays.toString(ints)));
          }
          processed.set(true);
        }
      }).count();
      if (processed.get()) {
        Assert.assertEquals(count, 10);
        break;
      }
    }
    Assert.assertTrue(processed.get(), "Data are not converted!");
    Assert.assertEquals(converter.getFrequency(), 1000, 0.1);
  }

  @Test
  public static void testVariableProperties() {
    Assert.assertEquals(AperOutVariable.CCR.getUnit(), Units.OHM);
    Assert.assertTrue(AperOutVariable.CCR.options().contains(Variable.Option.TEXT_VALUE_BANNER));

    EnumSet.of(AperOutVariable.R).forEach(variable -> Assert.assertEquals(variable.getUnit(), MetricPrefix.MILLI(Units.OHM)));
    EnumSet.of(AperOutVariable.R).forEach(variable -> Assert.assertTrue(variable.options().contains(Variable.Option.VISIBLE)));
  }

  @Test
  public static void testInputVariablesClass() {
    EnumSet.allOf(AperOutVariable.class).forEach(variable -> Assert.assertEquals(variable.getInputVariablesClass(), AperInVariable.class));
  }

  @DataProvider(name = "filter-delay")
  public static Object[][] filterDelay() {
    return new Object[][] {
        {AperOutVariable.R, 24.5},
        {AperOutVariable.CCR, 24.5},
    };
  }

  @Test(dataProvider = "filter-delay")
  public static void testFilterDelay(@Nonnull AperOutVariable variable, double delay) {
    Assert.assertEquals(variable.filter().getDelay(), delay, 0.001, variable.toString());
  }

  @Test(enabled = false)
  public static void testSplineSurface1() {
    SplineCoefficientsUtils.testSplineSurface1(AperSurfaceCoefficientsChannel1.class);
  }

  @Test(enabled = false)
  public static void testSplineSurface2() {
    SplineCoefficientsUtils.testSplineSurface2(AperSurfaceCoefficientsChannel2.class);
  }
}