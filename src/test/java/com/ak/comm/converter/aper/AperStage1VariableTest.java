package com.ak.comm.converter.aper;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.ToIntegerConverter;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public class AperStage1VariableTest {
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

            new int[] {100506, -546133, 1521, 16777215, -546132, 2000}},
    };
  }

  @Test(dataProvider = "variables")
  public void testApply(@Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    Converter<BufferFrame, AperStage1Variable> converter = new ToIntegerConverter<>(AperStage1Variable.class, 1000);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    converter.apply(bufferFrame).forEach(ints -> {
      Assert.assertEquals(ints, outputInts, "expected = %s, actual = %s".formatted(Arrays.toString(outputInts), Arrays.toString(ints)));
      processed.set(true);
    });
    Assert.assertEquals(converter.getFrequency(), 1000, 0.1);
  }

  @Test
  public void testVariableProperties() {
    EnumSet.of(AperStage1Variable.R1, AperStage1Variable.R2).forEach(t -> Assert.assertEquals(t.getUnit(), AbstractUnit.ONE));
    EnumSet.of(AperStage1Variable.E1, AperStage1Variable.E2).forEach(t -> Assert.assertEquals(t.getUnit(), MetricPrefix.MICRO(Units.VOLT), t.name()));
    EnumSet.of(AperStage1Variable.CCU1, AperStage1Variable.CCU2).forEach(t -> Assert.assertEquals(t.getUnit(), AbstractUnit.ONE));
  }
}