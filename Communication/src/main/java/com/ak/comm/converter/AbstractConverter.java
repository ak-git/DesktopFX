package com.ak.comm.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import static com.ak.comm.core.LogLevels.LOG_LEVEL_VALUES;

public abstract class AbstractConverter<RESPONSE, EV extends Enum<EV> & Variable<EV>> implements Converter<RESPONSE, EV> {
  private final Logger logger = Logger.getLogger(getClass().getName());
  @Nonnull
  private final List<EV> variables;

  public AbstractConverter(@Nonnull Class<EV> evClass) {
    variables = Collections.unmodifiableList(new ArrayList<>(EnumSet.allOf(evClass)));
  }

  @Override
  public final List<EV> variables() {
    return variables;
  }

  @Override
  public final Stream<int[]> apply(@Nonnull RESPONSE response) {
    return innerApply(response).peek(ints -> {
      if (ints.length != variables.size()) {
        logger.log(Level.WARNING, String.format("Invalid variables: %s not match %s", variables, Arrays.toString(ints)));
      }

      if (logger.isLoggable(LOG_LEVEL_VALUES)) {
        logger.log(LOG_LEVEL_VALUES, String.format("#%x [ %s ]", hashCode(),
            IntStream.iterate(0, operand -> operand + 1).limit(variables.size()).mapToObj(
                value -> String.format("%s = %d", variables.get(value), ints[value])).collect(Collectors.joining(", "))));
      }
    });
  }

  protected abstract Stream<int[]> innerApply(@Nonnull RESPONSE response);
}
