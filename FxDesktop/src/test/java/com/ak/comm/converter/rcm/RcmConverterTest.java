package com.ak.comm.converter.rcm;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.AbstractSplineCoefficientsChartApp;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.rcm.calibration.RcmCalibrationVariable;
import com.ak.numbers.rcm.RcmBaseSurfaceCoefficientsChannel1;
import com.ak.numbers.rcm.RcmBaseSurfaceCoefficientsChannel2;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.comm.converter.rcm.RcmInVariable.ECG_X;
import static com.ak.comm.converter.rcm.RcmInVariable.RHEO_1X;
import static com.ak.comm.converter.rcm.RcmInVariable.RHEO_2X;
import static com.ak.comm.converter.rcm.RcmOutVariable.BASE_1;
import static com.ak.comm.converter.rcm.RcmOutVariable.BASE_2;
import static com.ak.comm.converter.rcm.RcmOutVariable.ECG;
import static com.ak.comm.converter.rcm.RcmOutVariable.QS_1;
import static com.ak.comm.converter.rcm.RcmOutVariable.QS_2;
import static com.ak.comm.converter.rcm.RcmOutVariable.RHEO_1;
import static com.ak.comm.converter.rcm.RcmOutVariable.RHEO_2;
import static com.ak.comm.converter.rcm.calibration.RcmCalibrationVariable.AVG_RHEO_ADC;
import static com.ak.comm.converter.rcm.calibration.RcmCalibrationVariable.BASE_ADC;
import static com.ak.comm.converter.rcm.calibration.RcmCalibrationVariable.CC_ADC;
import static com.ak.comm.converter.rcm.calibration.RcmCalibrationVariable.MIN_RHEO_ADC;
import static com.ak.comm.converter.rcm.calibration.RcmCalibrationVariable.RHEO_ADC;

public final class RcmConverterTest {
  @DataProvider(name = "variables")
  public static Object[][] variables() {
    return new Object[][] {
        {
            new byte[] {-10, -36, -125, -72, -5, -60, -125, -124, -111, -94, -7, -98, -127, -128, -5, -78, -127, -10, -127, -128},
            new int[] {-67590, 218810, 0, 66, -38791, 1267364, 0}
        },
    };
  }

  @Test(dataProvider = "variables")
  public void testApply(@Nonnull byte[] inputBytes, @Nonnull int[] outputInts) {
    Converter<BufferFrame, RcmOutVariable> converter = new LinkedConverter<>(new RcmConverter(), RcmOutVariable.class);
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 2000 - 1; i++) {
      int finalI = i;
      long count = converter.apply(bufferFrame).peek(ints -> {
        if (finalI > 1900) {
          Assert.assertEquals(ints, outputInts, String.format("expected = %s, actual = %s", Arrays.toString(outputInts), Arrays.toString(ints)));
          processed.set(true);
        }
      }).count();
      if (processed.get()) {
        Assert.assertEquals(count, 40);
        break;
      }
    }
    Assert.assertTrue(processed.get(), "Data are not converted!");
    Assert.assertEquals(converter.getFrequency(), 200, 0.1);
  }

  @DataProvider(name = "calibrable-variables")
  public static Object[][] calibrableVariables() {
    return new Object[][] {
        {
            new byte[] {-10, -36, -125, -72, -5, -60, -125, -124, -111, -94, -7, -98, -127, -128, -5, -78, -127, -10, -127, -128},
            new int[] {0, 69, -274, -205, 0}
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
  public static void testInputVariablesClass() {
    EnumSet.of(RHEO_1X, RHEO_2X, ECG_X).forEach(variable -> Assert.assertTrue(variable.options().isEmpty(), variable.options().toString()));
    EnumSet.allOf(RcmOutVariable.class).forEach(variable -> Assert.assertEquals(variable.getInputVariablesClass(), RcmInVariable.class));
    EnumSet.of(RHEO_1, RHEO_2).forEach(variable -> Assert.assertEquals(variable.getUnit(), MetricPrefix.MICRO(Units.OHM)));
    EnumSet.of(RHEO_1, RHEO_2).forEach(variable -> Assert.assertTrue(variable.options().contains(Variable.Option.INVERSE)));
    EnumSet.of(RHEO_1, RHEO_2).forEach(variable -> Assert.assertTrue(variable.options().contains(Variable.Option.FORCE_ZERO_IN_RANGE)));
    EnumSet.of(BASE_1, BASE_2).forEach(variable -> Assert.assertEquals(variable.getUnit(), MetricPrefix.MILLI(Units.OHM)));
    EnumSet.of(QS_1, QS_2).forEach(variable -> Assert.assertEquals(variable.getUnit(), Units.OHM));
    EnumSet.of(ECG).forEach(variable -> Assert.assertEquals(variable.getUnit(), MetricPrefix.MILLI(Units.VOLT)));
    EnumSet.of(QS_1, QS_2).forEach(v -> Assert.assertEquals(v.options(), EnumSet.of(Variable.Option.TEXT_VALUE_BANNER), v.options().toString()));

    EnumSet.allOf(RcmCalibrationVariable.class).forEach(variable -> Assert.assertEquals(variable.getInputVariablesClass(), RcmInVariable.class));
    EnumSet.of(CC_ADC, BASE_ADC, RHEO_ADC).forEach(v -> Assert.assertTrue(v.options().contains(Variable.Option.VISIBLE), v.options().toString()));
    EnumSet.of(MIN_RHEO_ADC, AVG_RHEO_ADC).forEach(v -> Assert.assertTrue(v.options().contains(Variable.Option.TEXT_VALUE_BANNER), v.options().toString()));
  }

  @Test(enabled = false)
  public static void testBaseSplineSurface1() throws IOException {
    AbstractSplineCoefficientsChartApp.testSplineSurface1(RcmBaseSurfaceCoefficientsChannel1.class);
  }

  @Test(enabled = false)
  public static void testBaseSplineSurface2() throws IOException {
    AbstractSplineCoefficientsChartApp.testSplineSurface2(RcmBaseSurfaceCoefficientsChannel2.class);
  }
}