package com.ak.comm.converter;

import java.util.Arrays;
import java.util.stream.Stream;

public final class StringToIntegerConverter<V extends Enum<V> & Variable<V>> extends AbstractConverter<String, V> {
  public StringToIntegerConverter(Class<V> evClass, int frequency) {
    super(evClass, frequency);
  }

  @Override
  protected Stream<int[]> innerApply(String frame) {
    var values = new int[variables().size()];
    Arrays.fill(values, Integer.parseInt(frame, 16));
    return Stream.of(values);
  }
}
