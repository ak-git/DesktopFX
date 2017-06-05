package com.ak.comm.converter.aper.ecg;

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
import com.ak.comm.converter.aper.AperInVariable;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public final class AperECGConverterTest {
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

            new int[] {14999, 2, 997, 450000, 5, 1558}},
    };
  }

  @Test(dataProvider = "variables")
  public void testApply(@Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    Function<BufferFrame, Stream<int[]>> converter = new LinkedConverter<>(
        new ToIntegerConverter<>(AperInVariable.class), AperECGVariable.class);
    EnumSet.of(AperInVariable.R1, AperInVariable.R2).forEach(t -> Assert.assertEquals(t.getUnit(), AbstractUnit.ONE));
    EnumSet.of(AperInVariable.E1, AperInVariable.E2).forEach(t -> Assert.assertEquals(t.getUnit(), MetricPrefix.MICRO(Units.VOLT), t.name()));
    EnumSet.of(AperInVariable.RI1, AperInVariable.RI2).forEach(t -> Assert.assertEquals(t.getUnit(), Units.OHM));

    EnumSet.of(AperECGVariable.R1, AperECGVariable.R2).forEach(t -> Assert.assertEquals(t.getUnit(), MetricPrefix.MILLI(Units.OHM)));
    EnumSet.of(AperECGVariable.ECG1, AperECGVariable.ECG2).forEach(t -> Assert.assertEquals(t.getUnit(), MetricPrefix.MICRO(Units.VOLT)));
    EnumSet.of(AperECGVariable.RI1, AperECGVariable.RI2).forEach(t -> Assert.assertEquals(t.getUnit(), Units.OHM));

    Assert.assertEquals(AperECGVariable.R1.filter().toString(), AperECGVariable.R2.filter().toString());
    Assert.assertEquals(AperECGVariable.ECG1.filter().toString(), AperECGVariable.ECG2.filter().toString());

    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 200 - 1; i++) {
      long count = converter.apply(bufferFrame).count();
      Assert.assertTrue(count == 0 || count == 9 || count == 10, Long.toString(count));
    }
    Assert.assertEquals(converter.apply(bufferFrame).peek(ints -> {
      Assert.assertEquals(ints, outputInts, String.format("expected = %s, actual = %s", Arrays.toString(outputInts), Arrays.toString(ints)));
      processed.set(true);
    }).count(), 10);
    Assert.assertTrue(processed.get(), "Data are not converted!");
  }
}