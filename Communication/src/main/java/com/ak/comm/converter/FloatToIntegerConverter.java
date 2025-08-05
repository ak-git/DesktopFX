package com.ak.comm.converter;

import com.ak.comm.bytes.BufferFrame;

import java.util.stream.Stream;

public final class FloatToIntegerConverter<V extends Enum<V> & Variable<V>> extends AbstractConverter<BufferFrame, V> {
  public FloatToIntegerConverter(Class<V> evClass, int frequency) {
    super(evClass, frequency);
  }

  @Override
  protected Stream<int[]> innerApply(BufferFrame frame) {
    var values = new int[variables().size()];
    for (var i = 0; i < values.length; i++) {
      values[i] = Math.round(frame.getFloat(1 + i * Float.BYTES));
    }
    return Stream.of(values);
  }
}
