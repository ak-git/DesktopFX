package com.ak.comm.converter;

import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.util.LogUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.util.logging.Level.WARNING;

public final class ConverterTest {
  private enum NoVariables implements Variable<NoVariables> {
  }

  private enum ThreeVariables implements Variable<ThreeVariables> {
    TV1, TV2, TV3
  }

  private static final Converter<Integer, NoVariables> INVALID_CONVERTER =
      new AbstractConverter<Integer, NoVariables>(NoVariables.class) {
        @Override
        protected Stream<int[]> innerApply(@Nonnull Integer integer) {
          return Stream.of(new int[] {integer});
        }
      };
  private static final Logger LOGGER_INVALID = Logger.getLogger(INVALID_CONVERTER.getClass().getName());

  private static final Converter<Integer, NoVariables> VALID_CONVERTER_0 =
      new AbstractConverter<Integer, NoVariables>(NoVariables.class) {
        @Override
        protected Stream<int[]> innerApply(@Nonnull Integer integer) {
          return Stream.empty();
        }
      };
  private static final Logger LOGGER_VALID = Logger.getLogger(VALID_CONVERTER_0.getClass().getName());

  @Test
  public void testInvalidApply() {
    LogUtils.substituteLogLevel(LOGGER_INVALID, WARNING,
        () -> Assert.assertEquals(INVALID_CONVERTER.apply(1).count(), 1),
        logRecord -> Assert.assertEquals(logRecord.getMessage(), "Invalid variables: [] not match [1]"));
  }

  @Test
  public void testValidApply() {
    LogUtils.substituteLogLevel(LOGGER_VALID, WARNING,
        () -> Assert.assertEquals(VALID_CONVERTER_0.apply(1).count(), 0),
        logRecord -> Assert.fail(logRecord.getMessage()));
  }

  @Test
  public void testFilter() {
    Assert.assertEquals(VALID_CONVERTER_0.filter().size(), 1);
    Converter<Integer, ThreeVariables> converter = new AbstractConverter<Integer, ThreeVariables>(ThreeVariables.class) {
      @Override
      protected Stream<int[]> innerApply(@Nonnull Integer integer) {
        return Stream.of(new int[] {integer, integer * 2, integer * 3});
      }
    };
    Assert.assertEquals(converter.filter().size(), converter.variables().size());
  }
}