package com.ak.comm.converter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface Converter<R, V extends Enum<V> & Variable<V>> extends Function<R, Stream<int[]>>, Refreshable {
  @Nonnull
  List<V> variables();

  @Nonnegative
  double getFrequency();
}
