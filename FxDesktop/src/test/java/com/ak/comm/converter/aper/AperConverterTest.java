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

            (byte) 0xff, (byte) 0xff, (byte) 0xff, 0,
            5, 0, 0, 0,
            (byte) 0xd0, 0x07, 0, 0},

            new int[] {14974, -486109, 0, 808, 450000, -486108, 0, 1057}},
    };
  }

  @Test(dataProvider = "variables")
  public void testApply(@Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    Converter<BufferFrame, AperOutVariable> converter = new LinkedConverter<>(
        new ToIntegerConverter<>(AperInVariable.class, 1000), AperOutVariable.class);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 2000 - 1; i++) {
      long count = converter.apply(bufferFrame).count();
      Assert.assertTrue(count == 0 || count == 9 || count == 10, Long.toString(count));
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
    EnumSet.of(AperInVariable.R1, AperInVariable.R2).forEach(t -> Assert.assertEquals(t.getUnit(), AbstractUnit.ONE));
    EnumSet.of(AperInVariable.E1, AperInVariable.E2).forEach(t -> Assert.assertEquals(t.getUnit(), MetricPrefix.MICRO(Units.VOLT), t.name()));
    EnumSet.of(AperInVariable.CCU1, AperInVariable.CCU2).forEach(t -> Assert.assertEquals(t.getUnit(), AbstractUnit.ONE));

    EnumSet.of(AperOutVariable.R1, AperOutVariable.R2).forEach(t -> Assert.assertEquals(t.getUnit(), MetricPrefix.MILLI(Units.OHM)));
    EnumSet.of(AperOutVariable.ECG1, AperOutVariable.ECG2).forEach(t -> Assert.assertEquals(t.getUnit(), MetricPrefix.MICRO(Units.VOLT)));
    EnumSet.of(AperOutVariable.MYO1, AperOutVariable.MYO2).forEach(t -> Assert.assertEquals(t.getUnit(), MetricPrefix.MICRO(Units.VOLT)));

    EnumSet<AperOutVariable> serviceVars = EnumSet.of(AperOutVariable.CCR1, AperOutVariable.CCR2);
    serviceVars.forEach(t -> Assert.assertEquals(t.getUnit(), Units.OHM));

    Assert.assertEquals(AperOutVariable.R1.filter().toString(), AperOutVariable.R2.filter().toString());
    Assert.assertEquals(AperOutVariable.ECG1.filter().toString(), AperOutVariable.ECG2.filter().toString());
    Assert.assertEquals(AperOutVariable.MYO1.filter().toString(), AperOutVariable.MYO2.filter().toString());

    serviceVars.forEach(t -> Assert.assertFalse(t.options().contains(Variable.Option.VISIBLE)));
    EnumSet.complementOf(serviceVars).forEach(t -> Assert.assertTrue(t.options().contains(Variable.Option.VISIBLE), t.name()));
  }
}