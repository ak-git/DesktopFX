package com.ak.comm.converter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

public abstract class AbstractConverter<RESPONSE, EV extends Enum<EV> & Variable<EV>> implements Converter<RESPONSE, EV> {
  private static final Level LOG_LEVEL_VALUES = Level.FINE;
  private final Logger logger = Logger.getLogger(getClass().getName());
  @Nonnull
  private final List<EV> variables;

  public AbstractConverter(@Nonnull Class<EV> evClass) {
    variables = Collections.unmodifiableList(
        StreamSupport.stream(EnumSet.allOf(evClass).spliterator(), false).sorted().collect(Collectors.toList()));
  }

  @Override
  public final List<EV> variables() {
    return variables;
  }

  @Override
  public final Stream<int[]> apply(@Nonnull RESPONSE response) {
    Stream<int[]> stream = innerApply(response);
    if (logger.isLoggable(LOG_LEVEL_VALUES)) {
      stream = stream.peek(ints -> logger.log(LOG_LEVEL_VALUES, String.format("#%x [ %s ]", hashCode(),
          IntStream.iterate(0, operand -> operand + 1).limit(variables.size()).mapToObj(
              value -> String.format("%s = %d", variables.get(value), ints[value])).collect(Collectors.joining(", "))
      )));
    }
    return stream;
  }

  protected abstract Stream<int[]> innerApply(@Nonnull RESPONSE response);
}
