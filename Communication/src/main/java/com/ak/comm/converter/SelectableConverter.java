package com.ak.comm.converter;

import java.util.EnumSet;
import java.util.stream.Stream;

final class SelectableConverter<I extends Enum<I> & Variable<I>, O extends Enum<O> & DependentVariable<I, O>>
    extends AbstractConverter<Stream<int[]>, O> {
  SelectableConverter(Class<O> evClass, double frequency) {
    super(evClass, frequency,
        EnumSet.allOf(evClass).stream().map(DependentVariable::getInputVariables)
            .map(evs -> evs.stream().mapToInt(Enum::ordinal).toArray()).toList()
    );
  }

  @Override
  protected Stream<int[]> innerApply(Stream<int[]> response) {
    return response;
  }
}
