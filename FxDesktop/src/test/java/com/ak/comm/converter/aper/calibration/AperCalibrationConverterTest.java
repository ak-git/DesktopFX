package com.ak.comm.converter.aper.calibration;

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
import com.ak.comm.converter.aper.AperInVariable;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;

public final class AperCalibrationConverterTest {
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

            new int[] {1506, 99500, 1004, 281, 100506}},
    };
  }

  @Test(dataProvider = "variables")
  public void testApply(@Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    Converter<BufferFrame, AperCalibrationVariable> converter = new LinkedConverter<>(
        new ToIntegerConverter<>(AperInVariable.class, 1000), AperCalibrationVariable.class);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 100000 - 1; i++) {
      long count = converter.apply(bufferFrame).count();
      Assert.assertTrue(count == 0 || count == 1 || count == 9 || count == 10, String.format("Index %d, count %d", i, count));
    }
    Assert.assertEquals(converter.apply(bufferFrame).peek(ints -> {
      Assert.assertEquals(ints, outputInts, String.format("expected = %s, actual = %s", Arrays.toString(outputInts), Arrays.toString(ints)));
      processed.set(true);
    }).count(), 10);
    Assert.assertTrue(processed.get(), "Data are not converted!");
    Assert.assertEquals(converter.getFrequency(), 1000, 0.1);
  }

  @Test
  public static void testVariableProperties() {
    EnumSet.allOf(AperCalibrationVariable.class).forEach(t -> Assert.assertEquals(t.getUnit(), AbstractUnit.ONE));
    EnumSet.allOf(AperCalibrationVariable.class).forEach(variable -> Assert.assertEquals(variable.getInputVariablesClass(), AperInVariable.class));
    EnumSet.complementOf(EnumSet.of(AperCalibrationVariable.PU)).forEach(variable -> Assert.assertEquals(variable.options(),
        EnumSet.of(Variable.Option.TEXT_VALUE_BANNER), variable.name()));
    Assert.assertEquals(AperCalibrationVariable.PU.options(), EnumSet.of(Variable.Option.VISIBLE));
  }
}