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
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;

public class AperCalibrationCurrent1VariableTest {
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

            new int[] {790, 52223, 8717572, 100506, 16777215}
        },
    };
  }

  @Test(dataProvider = "variables")
  public void testApply(@Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    Converter<BufferFrame, AperCalibrationCurrent1Variable> converter = LinkedConverter.of(
        new ToIntegerConverter<>(AperStage1Variable.class, 1000), AperCalibrationCurrent1Variable.class);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 3000 - 1; i++) {
      long count = converter.apply(bufferFrame).peek(ints -> {
        if (!processed.get()) {
          Assert.assertEquals(ints, outputInts, "expected = %s, actual = %s".formatted(Arrays.toString(outputInts), Arrays.toString(ints)));
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
  public void testVariableProperties() {
    EnumSet.allOf(AperCalibrationCurrent1Variable.class).forEach(t -> Assert.assertEquals(t.getUnit(), AbstractUnit.ONE));
    EnumSet.allOf(AperCalibrationCurrent1Variable.class).forEach(variable -> Assert.assertEquals(variable.getInputVariablesClass(), AperStage1Variable.class));
    EnumSet.complementOf(EnumSet.of(AperCalibrationCurrent1Variable.PU_1, AperCalibrationCurrent1Variable.PU_2)).forEach(variable -> Assert.assertEquals(variable.options(),
        EnumSet.of(Variable.Option.TEXT_VALUE_BANNER), variable.name()));
    EnumSet.of(AperCalibrationCurrent1Variable.PU_1, AperCalibrationCurrent1Variable.PU_2).forEach(variable -> Assert.assertEquals(variable.options(),
        EnumSet.of(Variable.Option.VISIBLE), variable.name()));
  }
}