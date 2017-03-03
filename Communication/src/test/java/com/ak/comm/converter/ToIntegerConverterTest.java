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

public class ToIntegerConverterTest {
  private enum SingleVariable implements Variable {
    S
  }

  private ToIntegerConverterTest() {
  }

  @DataProvider(name = "variables")
  public static Object[][] variables() {
    return new Object[][] {
        {SingleVariable.class, new byte[] {1, 2, 3, 4, 5, 6},
            new int[] {2 + (3 << 8) + (4 << 16) + (5 << 24)}},
        {TwoVariables.class, new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9},
            new int[] {2 + (3 << 8) + (4 << 16) + (5 << 24), 6 + (7 << 8) + (8 << 16) + (9 << 24)}},
    };
  }

  @Test(dataProvider = "variables")
  public static <T extends Enum<T> & Variable> void testApply(@Nonnull Class<T> evClass, @Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    ToIntegerConverter<T> converter = new ToIntegerConverter<>(evClass);
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