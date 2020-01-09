package com.ak.comm.converter;

import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class SelectableConverter<I extends Enum<I> & Variable<I>, O extends Enum<O> & DependentVariable<I, O>>
    extends AbstractConverter<Stream<int[]>, O> {
  SelectableConverter(@Nonnull Class<O> evClass, @Nonnegative double frequency) {
    super(evClass, frequency, EnumSet.allOf(evClass).stream().map(DependentVariable::getInputVariables).
        map(evs -> evs.stream().mapToInt(Enum::ordinal).toArray()).collect(Collectors.toList()));
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull Stream<int[]> response) {
    return response;
  }
}
