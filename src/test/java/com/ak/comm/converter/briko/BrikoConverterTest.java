package com.ak.comm.converter.briko;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BrikoConverterTest {
  @DataProvider(name = "variables")
  public static Object[][] variables() {
    return new Object[][] {
        {new byte[] {
            0, 0x20,
            (byte) 0xc1,
            0x2d, 0x4f, 0x02, 0x00,
            (byte) 0xc2,
            0x5a, 0x27, 0x03, 0x00,
            (byte) 0xc3,
            0, 0, 0, 0,
            (byte) 0xc4,
            0, 0, 0, 0,
            (byte) 0xc5,
            0x20, (byte) 0xbf, 0x02, 0x00,
            (byte) 0xc6,
            0x20, (byte) 0xbf, 0x02, 0x00,
        },
            new int[] {2545, 3652, -480, -480, 180, 180}},
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
    Assert.assertEquals(converter.getFrequency(), 1000, 0.1);
  }
}