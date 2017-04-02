package com.ak.comm.converter.aper;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.numbers.Coefficients;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.AperCoefficients;
import com.ak.util.LineFileCollector;
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

            new int[] {14970, 997, 15000, 1558}},
    };
  }

  @Test(dataProvider = "variables")
  public void testApply(@Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    Function<BufferFrame, Stream<int[]>> converter = new LinkedConverter<>(new ToIntegerConverter<>(AperInVariable.class), AperOutVariable.class);
    EnumSet.of(AperInVariable.R1, AperInVariable.R2).forEach(t -> Assert.assertEquals(t.getUnit(), AbstractUnit.ONE));
    EnumSet.of(AperInVariable.M1, AperInVariable.M2).forEach(t -> Assert.assertEquals(t.getUnit(), MetricPrefix.MILLI(Units.VOLT), t.name()));
    EnumSet.of(AperInVariable.RI1, AperInVariable.RI2).forEach(t -> Assert.assertEquals(t.getUnit(), AbstractUnit.ONE));

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

  @Test(enabled = false)
  public static void test() throws IOException {
    IntBinaryOperator function = Interpolators.interpolator(new Coefficients[] {AperCoefficients.IADC_VADC_0, AperCoefficients.IADC_VADC_15000}).get();

    Assert.assertNull(IntStream.rangeClosed(0, 1).mapToObj(y -> IntStream.rangeClosed(0, 1).map(x -> function.applyAsInt(x, y))).
        map(stream -> stream.mapToObj(value -> String.format("%d", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector(Paths.get("out.txt"), LineFileCollector.Direction.VERTICAL)));
  }
}