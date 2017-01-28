package com.ak.comm.converter;

import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.util.LogUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.util.logging.Level.WARNING;

public final class ConverterTest {
  private enum SingleVariable implements Variable {
    SINGLE_VARIABLE
  }

  private static final Converter<Integer, TwoVariables> INVALID_CONVERTER =
      new AbstractConverter<Integer, TwoVariables>(TwoVariables.class) {
        @Override
        protected Stream<int[]> innerApply(@Nonnull Integer integer) {
          return Stream.of(new int[] {integer});
        }
      };
  private static final Logger LOGGER_INVALID = Logger.getLogger(INVALID_CONVERTER.getClass().getName());

  private static final Converter<Integer, SingleVariable> VALID_CONVERTER_0 =
      new AbstractConverter<Integer, SingleVariable>(SingleVariable.class) {
        @Override
        protected Stream<int[]> innerApply(@Nonnull Integer integer) {
          return Stream.empty();
        }
      };
  private static final Logger LOGGER_VALID = Logger.getLogger(VALID_CONVERTER_0.getClass().getName());

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidApply() {
    LogUtils.substituteLogLevel(LOGGER_INVALID, WARNING,
        () -> Assert.assertEquals(INVALID_CONVERTER.apply(1).count(), 1),
        logRecord -> Assert.assertEquals(logRecord.getMessage(), "Invalid variables: [V1, V2] not match [1]"));
  }

  @Test
  public void testValidApply() {
    LogUtils.substituteLogLevel(LOGGER_VALID, WARNING,
        () -> Assert.assertEquals(VALID_CONVERTER_0.apply(1).count(), 0),
        logRecord -> Assert.fail(logRecord.getMessage()));
  }
}