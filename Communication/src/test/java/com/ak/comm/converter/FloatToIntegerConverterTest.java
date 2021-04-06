package com.ak.comm.converter;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;

public class FloatToIntegerConverterTest {
  @DataProvider(name = "variables")
  public static Object[][] variables() {
    return new Object[][] {
        {ADCVariable.class, new byte[] {3, 0, 0, (byte) 0x80, 0x3f},
            new int[] {1}},
        {TwoVariables.class, new byte[] {3, 0, 0, (byte) 0x80, 0x3f, 0, 0, (byte) 0x80, 0x3f},
            new int[] {1, 1}},
    };
  }

  @Test(dataProvider = "variables")
  public <T extends Enum<T> & Variable<T>> void testApply(@Nonnull Class<T> evClass, @Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    FloatToIntegerConverter<T> converter = new FloatToIntegerConverter<>(evClass, 1000);
    Assert.assertEquals(EnumSet.allOf(evClass), converter.variables());
    EnumSet.allOf(evClass).forEach(t -> Assert.assertEquals(t.getUnit(), AbstractUnit.ONE));
    AtomicBoolean processed = new AtomicBoolean();
    converter.apply(new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN)).
        forEach(ints -> {
          Assert.assertEquals(ints, outputInts, Arrays.toString(ints));
          processed.set(true);
        });
    Assert.assertTrue(processed.get(), "Data are not converted!");
  }
}