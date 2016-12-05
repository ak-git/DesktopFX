package com.ak.comm.converter;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public abstract class AbstractConverter<RESPONSE> implements Converter<RESPONSE> {
  private static final Level LOG_LEVEL_VALUES = Level.FINE;
  private final Logger logger = Logger.getLogger(getClass().getName());

  @Override
  public final Stream<int[]> apply(@Nonnull RESPONSE response) {
    Stream<int[]> stream = innerApply(response);
    if (logger.isLoggable(LOG_LEVEL_VALUES)) {
      stream = stream.peek(ints -> logger.log(LOG_LEVEL_VALUES, String.format("#%x %s", hashCode(), Arrays.toString(ints))));
    }
    return stream;
  }

  protected abstract Stream<int[]> innerApply(@Nonnull RESPONSE response);
}
