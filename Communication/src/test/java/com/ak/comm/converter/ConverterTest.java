package com.ak.comm.converter;

import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.core.LogLevelSubstitution;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.util.logging.Level.WARNING;

public final class ConverterTest {
  private enum NoVariables implements Variable<NoVariables> {
  }

  private static final Converter<Integer, NoVariables> INVALID_CONVERTER =
      new AbstractConverter<Integer, NoVariables>(NoVariables.class) {
        @Override
        protected Stream<int[]> innerApply(@Nonnull Integer integer) {
          return Stream.of(new int[] {integer});
        }
      };
  private static final Logger LOGGER_INVALID = Logger.getLogger(INVALID_CONVERTER.getClass().getName());

  private static final Converter<Integer, NoVariables> VALID_CONVERTER =
      new AbstractConverter<Integer, NoVariables>(NoVariables.class) {
        @Override
        protected Stream<int[]> innerApply(@Nonnull Integer integer) {
          return Stream.empty();
        }
      };
  private static final Logger LOGGER_VALID = Logger.getLogger(VALID_CONVERTER.getClass().getName());

  @Test
  public void testInvalidApply() {
    LogLevelSubstitution.substituteLogLevel(LOGGER_INVALID, WARNING,
        () -> Assert.assertEquals(INVALID_CONVERTER.apply(1).count(), 1),
        logRecord -> Assert.assertEquals(logRecord.getMessage(), "Invalid variables: [] not match [1]"));
  }

  @Test
  public void testValidApply() {
    LogLevelSubstitution.substituteLogLevel(LOGGER_VALID, WARNING,
        () -> Assert.assertEquals(VALID_CONVERTER.apply(1).count(), 0),
        logRecord -> Assert.fail(logRecord.getMessage()));
  }
}