package com.ak.comm.converter;

import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public interface Converter<RESPONSE, EV extends Enum<EV> & Variable<EV>> extends Function<RESPONSE, Stream<int[]>> {
  @Nonnull
  SortedSet<EV> variables();
}
