package com.ak.comm.converter.rcm.calibration;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.rcm.RcmConverter;
import com.ak.comm.converter.rcm.RcmInVariable;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.comm.converter.rcm.calibration.RcmCalibrationVariable.AVG_RHEO_ADC;
import static com.ak.comm.converter.rcm.calibration.RcmCalibrationVariable.BASE_ADC;
import static com.ak.comm.converter.rcm.calibration.RcmCalibrationVariable.CC_ADC;
import static com.ak.comm.converter.rcm.calibration.RcmCalibrationVariable.MIN_RHEO_ADC;
import static com.ak.comm.converter.rcm.calibration.RcmCalibrationVariable.RHEO_ADC;

public final class RcmCalibrationConverterTest {
  @DataProvider(name = "calibrable-variables")
  public static Object[][] calibrableVariables() {
    return new Object[][] {
        {
            new byte[] {-10, -36, -125, -72, -5, -60, -125, -124, -111, -94, -7, -98, -127, -128, -5, -78, -127, -10, -127, -128},
            new int[] {0, 92, -274, -274, 0}
        },
    };
  }

  @Test(dataProvider = "calibrable-variables")
  public void testApplyCalibrator(@Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    Converter<BufferFrame, RcmCalibrationVariable> converter = new LinkedConverter<>(new RcmConverter(), RcmCalibrationVariable.class);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 800 - 1; i++) {
      long count = converter.apply(bufferFrame).count();
      Assert.assertTrue(count == 0 || count == 1, String.format("index %d, count %d", i, count));
    }
    Assert.assertEquals(converter.apply(bufferFrame).peek(ints -> {
      Assert.assertEquals(ints, outputInts, String.format("expected = %s, actual = %s", Arrays.toString(outputInts), Arrays.toString(ints)));
      processed.set(true);
    }).count(), 1);
    Assert.assertTrue(processed.get(), "Data are not converted!");
    Assert.assertEquals(converter.getFrequency(), 200, 0.1);
  }

  @Test
  public static void testVariables() {
    EnumSet.allOf(RcmCalibrationVariable.class).forEach(variable -> Assert.assertEquals(variable.getInputVariablesClass(), RcmInVariable.class));
    EnumSet.of(CC_ADC, BASE_ADC, RHEO_ADC).forEach(v -> Assert.assertTrue(v.options().contains(Variable.Option.VISIBLE), v.options().toString()));
    EnumSet.of(MIN_RHEO_ADC, AVG_RHEO_ADC).forEach(v -> Assert.assertTrue(v.options().contains(Variable.Option.TEXT_VALUE_BANNER), v.options().toString()));
  }
}