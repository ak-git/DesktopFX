package com.ak.comm.converter.aper;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.ToIntegerConverter;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public final class AperConverterTest {
  @DataProvider(name = "variables")
  public static Object[][] variables() {
    return new Object[][] {
        {new byte[] {1,
            (byte) 0x9a, (byte) 0x88, 0x01, 0,
            2, 0, 0, 0,
            (byte) 0xf1, 0x05, 0, 0,

            0x40, 0x0d, 0x03, 0,
            5, 0, 0, 0,
            (byte) 0xd0, 0x07, 0, 0},

            new int[] {14995, 997, 29771, 1558}},
    };
  }

  @Test(dataProvider = "variables")
  public void testApply(@Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    Function<BufferFrame, Stream<int[]>> converter = new LinkedConverter<>(new ToIntegerConverter<>(AperInVariable.class), AperOutVariable.class);
    EnumSet.of(AperInVariable.R1, AperInVariable.R2).forEach(t -> Assert.assertEquals(t.getUnit(), AbstractUnit.ONE));
    EnumSet.of(AperInVariable.M1, AperInVariable.M2).forEach(t -> Assert.assertEquals(t.getUnit(), MetricPrefix.MILLI(Units.VOLT), t.name()));
    EnumSet.of(AperInVariable.RI1, AperInVariable.RI2).forEach(t -> Assert.assertEquals(t.getUnit(), Units.OHM));

    EnumSet.of(AperOutVariable.R1).forEach(t -> Assert.assertEquals(t.getUnit(), MetricPrefix.MILLI(Units.OHM)));
    EnumSet.of(AperOutVariable.RI1).forEach(t -> Assert.assertEquals(t.getUnit(), Units.OHM));

    EnumSet.of(AperOutVariable.R2).forEach(t -> Assert.assertEquals(t.getUnit(), MetricPrefix.MILLI(Units.OHM)));
    EnumSet.of(AperOutVariable.RI2).forEach(t -> Assert.assertEquals(t.getUnit(), Units.OHM));

    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 100; i++) {
      converter.apply(bufferFrame);
    }
    converter.apply(bufferFrame).forEach(ints -> {
      Assert.assertEquals(ints, outputInts, String.format("expected = %s, actual = %s", Arrays.toString(outputInts), Arrays.toString(ints)));
      processed.set(true);
    });
    Assert.assertTrue(processed.get(), "Data are not converted!");
  }
}