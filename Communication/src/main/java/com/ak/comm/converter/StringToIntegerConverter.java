package com.ak.comm.converter;

import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public final class StringToIntegerConverter<V extends Enum<V> & Variable<V>> extends AbstractConverter<String, V> {
  public StringToIntegerConverter(@Nonnull Class<V> evClass, @Nonnegative int frequency) {
    super(evClass, frequency);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull String frame) {
    var values = new int[variables().size()];
    for (var i = 0; i < values.length; i++) {
      values[i] = Integer.parseInt(frame, 16);
    }
    return Stream.of(values);
  }
}
