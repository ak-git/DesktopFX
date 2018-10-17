package com.ak.comm.converter;

import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.util.LogUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.util.logging.Level.WARNING;

public class ConverterTest {
  private static final Converter<Integer, TwoVariables> INVALID_CONVERTER =
      new AbstractConverter<>(TwoVariables.class, 200) {
        @Override
        protected Stream<int[]> innerApply(@Nonnull Integer integer) {
          return Stream.of(new int[] {integer});
        }
      };
  private static final Logger LOGGER_INVALID = Logger.getLogger(INVALID_CONVERTER.getClass().getName());
  private static final Converter<Integer, ADCVariable> VALID_CONVERTER_0 =
      new AbstractConverter<>(ADCVariable.class, 1000) {
        @Override
        protected Stream<int[]> innerApply(@Nonnull Integer integer) {
          return Stream.empty();
        }
      };
  private static final Logger LOGGER_VALID = Logger.getLogger(VALID_CONVERTER_0.getClass().getName());

  private ConverterTest() {
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testInvalidApply() {
    Assert.assertTrue(LogUtils.isSubstituteLogLevel(LOGGER_INVALID, WARNING,
        () -> Assert.assertEquals(INVALID_CONVERTER.apply(1).count(), 1),
        logRecord -> Assert.assertEquals(logRecord.getMessage(), "Invalid variables: [V1, V2] not match [1]")));
  }

  @Test
  public static void testValidApply() {
    Assert.assertFalse(LogUtils.isSubstituteLogLevel(LOGGER_VALID, WARNING,
        () -> Assert.assertEquals(VALID_CONVERTER_0.apply(1).count(), 0),
        logRecord -> Assert.fail(logRecord.getMessage())));
  }

  @Test
  public static void testFrequencies() {
    Assert.assertEquals(INVALID_CONVERTER.getFrequency(), 200, 0.1);
    Assert.assertEquals(VALID_CONVERTER_0.getFrequency(), 1000, 0.1);
  }
}