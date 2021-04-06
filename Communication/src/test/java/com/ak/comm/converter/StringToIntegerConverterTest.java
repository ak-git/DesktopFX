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

public class StringToIntegerConverterTest {
  @DataProvider(name = "variables")
  public static Object[][] variables() {
    return new Object[][] {
        {ADCVariable.class, new byte[] {'\n', 51, 102, 102, 53, '\r',},
            new int[] {16373}},
        {TwoVariables.class, new byte[] {'\n', 51, 102, 102, 53, 51, 102, 102, 53, '\r',},
            new int[] {16373, 16373}},
    };
  }

  @Test(dataProvider = "variables")
  public <T extends Enum<T> & Variable<T>> void testApply(@Nonnull Class<T> evClass, @Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    StringToIntegerConverter<T> converter = new StringToIntegerConverter<>(evClass, 1000);
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