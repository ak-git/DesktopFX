package com.ak.comm.converter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.core.Refreshable;

public interface Converter<RESPONSE, EV extends Enum<EV> & Variable<EV>> extends Function<RESPONSE, Stream<int[]>>, Refreshable {
  @Nonnull
  List<EV> variables();

  @Nonnegative
  double getFrequency();
}
