package com.ak.comm.converter.briko;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.unit.Units;

public class BrikoConverterTest {
  @DataProvider(name = "variables")
  public static Object[][] variables() {
    return new Object[][] {
        {new byte[] {
            0, 0, 0,
            0, 0, 0, 2,
            0,
            0, 0, 0, 3,
            0,
            0, 0, 0, 4
        },
            new int[] {670607, 1006152, 1341696}},
    };
  }

  @Test(dataProvider = "variables")
  public void testApply(@Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    Converter<BufferFrame, BrikoVariable> converter = new BrikoConverter();
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);

    converter.apply(bufferFrame).forEach(ints -> {
      Assert.assertEquals(ints, outputInts, String.format("expected = %s, actual = %s", Arrays.toString(outputInts), Arrays.toString(ints)));
      processed.set(true);
    });

    Assert.assertTrue(processed.get(), "Data are not converted!");
    Assert.assertEquals(converter.getFrequency(), 100, 0.1);
  }

  @Test
  public static void testVariableProperties() {
    EnumSet.allOf(BrikoVariable.class).forEach(t -> Assert.assertEquals(t.getUnit(), Units.GRAM));
  }
}