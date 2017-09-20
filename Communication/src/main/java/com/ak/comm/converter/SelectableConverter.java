package com.ak.comm.converter;

import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class SelectableConverter<IN extends Enum<IN> & Variable<IN>, OUT extends Enum<OUT> & DependentVariable<IN, OUT>>
    extends AbstractConverter<Stream<int[]>, OUT> {
  SelectableConverter(@Nonnull Class<OUT> evClass, @Nonnegative double frequency) {
    super(evClass, frequency, EnumSet.allOf(evClass).stream().map(ev -> ev.getInputVariables()).
        map(evs -> evs.stream().mapToInt(Enum::ordinal).toArray()).collect(Collectors.toList()));
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull Stream<int[]> response) {
    return response;
  }
}
